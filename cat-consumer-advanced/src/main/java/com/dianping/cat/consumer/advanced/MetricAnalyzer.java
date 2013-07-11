package com.dianping.cat.consumer.advanced;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.spi.internal.ABTestCodec;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.advanced.dal.BusinessReport;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.metric.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.ReportConstants;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class MetricAnalyzer extends AbstractMessageAnalyzer<MetricReport> implements LogEnabled {
	public static final String ID = "metric";

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private BusinessReportDao m_businessReportDao;

	@Inject
	private MetricConfigManager m_configManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private ABTestCodec m_codec;

	@Inject
	private TaskManager m_taskManager;

	// key is project line,such as tuangou
	private Map<String, MetricReport> m_reports = new HashMap<String, MetricReport>();

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public MetricReport getReport(String product) {
		MetricReport report = m_reports.get(product);

		if (report == null) {
			report = new MetricReport(product);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}
		return report;
	}

	protected void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "metric");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				MetricReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getProduct(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading metric reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	private Map<String, String> parseABtests(Transaction transaction) {
		String abtest = queryAbTest(transaction);

		return parseABTests(abtest);
	}

	private String queryAbTest(Transaction transaction) {
		List<Message> messages = transaction.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				if ("URL".equals(message.getType()) && "ABTest".equals(message.getName())) {
					String data = (String) message.getData();

					return data;
				}
			}
		}
		return "";
	}

	public Map<String, String> parseABTests(String str) {
		// -1 is the all metric,design for default
		Map<String, String> abtests = new HashMap<String, String>();
		abtests.put("-1", "");

		abtests.putAll(m_codec.decode(str));
		return abtests;
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		String product = m_productLineConfigManager.queryProductLineByDomain(domain);
		MetricReport report = m_reports.get(product);

		if (report == null) {
			report = new MetricReport(product);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(product, report);
		}

		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			processMetricOnTransaction(product, report, (Transaction) message, tree);
		}
		if (message instanceof Transaction) {
			processTransaction(product, report, tree, (Transaction) message);
		} else if (message instanceof Metric) {
			processMetric(product, report, tree, (Metric) message);
		}
	}

	private int processMetric(String group, MetricReport report, MessageTree tree, Metric metric) {
		String type = metric.getType();
		String name = metric.getName();
		String domain = tree.getDomain();
		String data = (String) metric.getData();
		String status = metric.getStatus();
		ConfigItem config = parseValue(status, data);

		if (config != null) {
			long current = metric.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			String key = m_configManager.buildMetricKey(domain, "Metric", name);
			MetricItem metricItem = report.findOrCreateMetricItem(key);
			Map<String, String> abtests = parseABTests(type);

			metricItem.addDomain(domain).setType(status);
			updateMetric(metricItem, abtests, min, config.getCount(), config.getValue());

			config.setTitle(name);
			m_configManager.insertIfNotExist(domain, "Metric", name, config);
		}
		return 0;
	}

	private ConfigItem parseValue(String status, String data) {
		ConfigItem config = new ConfigItem();

		if ("C".equals(status)) {
			int count = Integer.parseInt(data);

			config.setCount(count);
			config.setValue((double) count);
			config.setShowCount(true);
		} else if ("T".equals(status)) {
			double duration = Double.parseDouble(data);

			config.setCount(1);
			config.setValue(duration);
			config.setShowAvg(true);
		} else if ("S".equals(status)) {
			double sum = Double.parseDouble(data);

			config.setCount(1);
			config.setValue(sum);
			config.setShowSum(true);
		} else if ("S,C".equals(status)) {
			String[] datas = data.split(",");

			config.setCount(Integer.parseInt(datas[0]));
			config.setValue(Double.parseDouble(datas[1]));
			config.setShowCount(true);
			config.setShowSum(true);
		} else {
			return null;
		}

		return config;
	}

	private int processTransaction(String group, MetricReport report, MessageTree tree, Transaction t) {
		int count = 0;
		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				count += processTransaction(group, report, tree, (Transaction) child);
			} else if (child instanceof Metric) {
				count += processMetric(group, report, tree, (Metric) child);
			}
		}

		return count;
	}

	private void processMetricOnTransaction(String product, MetricReport report, Transaction transaction,
	      MessageTree tree) {
		String type = transaction.getType();

		if (type.equals("Service")) {
			type = "PigeonService";
		}
		if ("URL".equals(type) || "PigeonService".equals(type)) {
			String name = transaction.getName();
			String domain = tree.getDomain();
			String key = m_configManager.buildMetricKey(domain, type, name);
			MetricItemConfig config = m_configManager.queryMetricItemConfig(key);

			if (config != null) {
				long current = transaction.getTimestamp() / 1000 / 60;
				int min = (int) (current % (60));
				double sum = transaction.getDurationInMicros();
				MetricItem metricItem = report.findOrCreateMetricItem(key);
				Map<String, String> abtests = parseABtests(transaction);

				metricItem.addDomain(domain).setType("C");
				updateMetric(metricItem, abtests, min, 1, sum);
			}
		}
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "metric");

			for (MetricReport report : m_reports.values()) {
				try {
					String xml = builder.buildXml(report);
					String product = report.getProduct();

					reportBucket.storeById(product, xml);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				int binary = 1;

				for (MetricReport report : m_reports.values()) {
					try {
						BusinessReport r = m_businessReportDao.createLocal();
						String product = report.getProduct();

						r.setName(ID);
						r.setProductLine(product);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(binary);
						r.setContent(DefaultNativeBuilder.build(report));
						r.setCreationDate(new Date());

						m_businessReportDao.insert(r);
					} catch (Throwable e) {
						m_logger.error(report.toString());
						t.setStatus(e);
						Cat.getProducer().logError(e);
					}
				}
				//for create baseline for metric
				m_taskManager.createTask(period, ReportConstants.CAT, ID, TaskProlicy.DAILY);
			}
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing metric reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	private void updateMetric(MetricItem metricItem, Map<String, String> abtests, int minute, int count, double sum) {
		for (Entry<String, String> entry : abtests.entrySet()) {
			Abtest abtest = metricItem.findOrCreateAbtest(entry.getKey());
			Group group = abtest.findOrCreateGroup(entry.getValue());
			Point point = group.findOrCreatePoint(minute);

			point.setCount(point.getCount() + count);
			point.setSum(point.getSum() + sum);
			point.setAvg(point.getSum() / point.getCount());
		}
	}

	public static class ConfigItem {
		private int m_count;

		private double m_value;

		private boolean m_showCount = false;

		private boolean m_showAvg = false;

		private boolean m_showSum = false;

		private String m_title;

		public String getTitle() {
			return m_title;
		}

		public void setTitle(String title) {
			m_title = title;
		}

		public int getCount() {
			return m_count;
		}

		public ConfigItem setCount(int count) {
			m_count = count;
			return this;
		}

		public double getValue() {
			return m_value;
		}

		public ConfigItem setValue(double value) {
			m_value = value;
			return this;
		}

		public boolean isShowCount() {
			return m_showCount;
		}

		public ConfigItem setShowCount(boolean showCount) {
			m_showCount = showCount;
			return this;
		}

		public boolean isShowAvg() {
			return m_showAvg;
		}

		public ConfigItem setShowAvg(boolean showAvg) {
			m_showAvg = showAvg;
			return this;
		}

		public boolean isShowSum() {
			return m_showSum;
		}

		public ConfigItem setShowSum(boolean showSum) {
			m_showSum = showSum;
			return this;
		}
	}
}
