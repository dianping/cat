package com.dianping.cat.consumer.advanced;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	public MetricReport getReport(String group) {
		MetricReport report = m_reports.get(group);

		if (report == null) {
			report = new MetricReport(group);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}

		report.getGroupNames().addAll(m_reports.keySet());
		return report;
	}

	protected void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "metric");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				MetricReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getGroup(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading metric reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		String group = m_configManager.getGroup(domain);
		MetricReport report = m_reports.get(group);

		if (report == null) {
			report = new MetricReport(group);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(group, report);
		}

		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			processUrl(group, report, (Transaction) message, tree);
		}
		if (message instanceof Transaction) {
			processTransaction(group, report, tree, (Transaction) message);
		} else if (message instanceof Metric) {
			processMetric(group, report, tree, (Metric) message);
		}
	}

	private void processUrl(String group, MetricReport report, Transaction transaction, MessageTree tree) {
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

				com.dianping.cat.consumer.metric.model.entity.Metric metric = report.findOrCreateMetric(name);
				Point point = metric.findOrCreatePoint(min);

				point.setCount(point.getCount() + 1);
				point.setSum(point.getSum() + transaction.getDurationInMillis());
				point.setAvg(point.getSum() / point.getCount());

				String childKey = config.getClassifications();
				if (childKey != null && childKey.length() > 0) {
					Object data = transaction.getData();
					if (data != null) {
						String channel = parseValue(childKey, (String) data);
						if (channel != null) {
							updateMetricChild(min, metric, childKey, channel, transaction.getDurationInMillis());
						}
					}
				}
			}
		}
	}

	private void updateMetricChild(int min, com.dianping.cat.consumer.metric.model.entity.Metric metric,
	      String childKey, String childValue, double value) {
		com.dianping.cat.consumer.metric.model.entity.Metric detail = metric.findOrCreateMetric(childKey + "="
		      + childValue);

		Point channelPoint = detail.findOrCreatePoint(min);

		channelPoint.setCount(channelPoint.getCount() + 1);
		channelPoint.setSum(channelPoint.getSum() + value);
		channelPoint.setAvg(channelPoint.getSum() / channelPoint.getCount());
	}

	private int processMetric(String group, MetricReport report, MessageTree tree, Metric metric) {
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

				com.dianping.cat.consumer.metric.model.entity.Metric temp = report.findOrCreateMetric(name);
				Point point = temp.findOrCreatePoint(min);

				point.setCount(point.getCount() + 1);
				point.setSum(point.getSum() + value);
				point.setAvg(point.getSum() / point.getCount());

				String childKey = config.getClassifications();
				if (childKey != null && childKey.length() > 0) {
					if (data != null) {
						String childValue = parseValue(childKey, data);
						if (childValue != null) {
							updateMetricChild(min, temp, childKey, childValue, value);
						}
					}
				}
			}
		}
		return 0;
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

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "metric");

			for (MetricReport report : m_reports.values()) {
				try {
					Set<String> groups = report.getGroupNames();

					groups.clear();
					groups.addAll(m_reports.keySet());

					String xml = builder.buildXml(report);
					String domain = report.getGroup();

					reportBucket.storeById(domain, xml);
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
						String group = report.getGroup();

						r.setName(ID);
						r.setProductLine(group);
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
}
