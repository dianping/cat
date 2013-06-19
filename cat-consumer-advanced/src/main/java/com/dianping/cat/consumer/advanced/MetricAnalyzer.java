package com.dianping.cat.consumer.advanced;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.advanced.BussinessConfigManager.BusinessConfig;
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
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class MetricAnalyzer extends AbstractMessageAnalyzer<MetricReport> implements LogEnabled {
	public static final String ID = "metric";

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private BusinessReportDao m_businessReportDao;

	@Inject
	private BussinessConfigManager m_configManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

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

	@Override
	public Set<String> getDomains() {
		return m_reports.keySet();
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

	private Map<Integer, String> parseABtests(Transaction transaction) {
		Map<Integer, String> abtests = new HashMap<Integer, String>();
		abtests.put(-1, "");
		double d = Math.random();
		String group = "";
		if (d > 0.9) {
			group = "C";
		} else if (d > 0.6) {
			group = "B";
		} else {
			group = "A";
		}
		abtests.put(1, group);
		abtests.put(2, group);
		abtests.put(3, group);
		return abtests;
	}

	public Map<Integer, String> parseABTests(String str) {
		Map<Integer, String> abtests = new HashMap<Integer, String>();
		abtests.put(-1, "");
		double d = Math.random();
		String group = "";
		if (d > 0.9) {
			group = "C";
		} else if (d > 0.6) {
			group = "B";
		} else {
			group = "A";
		}
		abtests.put(1, group);
		abtests.put(2, group);
		abtests.put(3, group);
		return abtests;
	}

	public String parseValue(final String key, final String data) {
		int len = data == null ? 0 : data.length();
		int keyLen = key.length();
		StringBuilder name = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean inName = true;

		for (int i = 0; i < len; i++) {
			char ch = data.charAt(i);

			switch (ch) {
			case '&':
				if (name.length() == keyLen && name.toString().equals(key)) {
					return value.toString();
				}

				inName = true;
				name.setLength(0);
				value.setLength(0);
				break;
			case '=':
				if (inName) {
					inName = false;
				} else {
					value.append(ch);
				}
				break;
			default:
				if (inName) {
					name.append(ch);
				} else {
					value.append(ch);
				}
				break;
			}
		}

		if (name.length() == keyLen && name.toString().equals(key)) {
			return value.toString();
		}

		return null;
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
			processUrl(product, report, (Transaction) message, tree);
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
		Map<String, BusinessConfig> configs = m_configManager.getMetricConfigs(domain);
		BusinessConfig config = configs.get(name);

		if (config != null) {
			String data = (String) metric.getData();
			String valueStr = parseValue(config.getTarget(), data);

			if (valueStr != null) {
				double value = Double.parseDouble(valueStr);
				long current = metric.getTimestamp() / 1000 / 60;
				int min = (int) (current % (60));
				MetricItem metricItem = report.findOrCreateMetricItem(name);
				Map<Integer, String> abtests = parseABTests(type);
				updateMetric(metricItem, domain, abtests, min, value);
			}
		}
		return 0;
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

	private void processUrl(String product, MetricReport report, Transaction transaction, MessageTree tree) {
		String type = transaction.getType();

		if (CatConstants.TYPE_URL.equals(type)) {
			String name = transaction.getName();
			String domain = tree.getDomain();
			Map<String, BusinessConfig> configs = m_configManager.getUrlConfigs(domain);
			BusinessConfig config = null;

			config = configs.get(name);
			if (config != null) {
				long current = transaction.getTimestamp() / 1000 / 60;
				int min = (int) (current % (60));
				double value = transaction.getDurationInMicros();
				MetricItem metricItem = report.findOrCreateMetricItem(name);

				Map<Integer, String> abtests = parseABtests(transaction);
				updateMetric(metricItem, tree.getDomain(), abtests, min, value);
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

	private void updateMetric(MetricItem metricItem, String domain, Map<Integer, String> abtests, int min, double value) {
		for (Entry<Integer, String> entry : abtests.entrySet()) {
			Abtest abtest = metricItem.findOrCreateAbtest(entry.getKey());
			Group group = abtest.findOrCreateGroup(entry.getValue());
			Point point = group.findOrCreatePoint(min);

			point.setCount(point.getCount() + 1);
			point.setSum(point.getSum() + value);
			point.setAvg(point.getSum() / point.getCount());
		}
	}
}
