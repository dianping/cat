package com.dianping.cat.consumer.matrix;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.matrix.model.transform.DefaultXmlBuilder;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class MatrixAnalyzer extends AbstractMessageAnalyzer<MatrixReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@SuppressWarnings("unused")
   @Inject
	private TaskDao m_taskDao;

	private Map<String, MatrixReport> m_reports = new HashMap<String, MatrixReport>();

	private long m_extraTime;

	private long m_startTime;

	private long m_duration;

	private Logger m_logger;

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

	public MatrixReport getReport(String domain) {
		MatrixReport report = m_reports.get(domain);

		if (report == null) {
			report = new MatrixReport(domain);
		}

		report.getDomainNames().addAll(m_reports.keySet());
		return report;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	private void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "matrix");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				MatrixReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading matrix reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		MatrixReport report = m_reports.get(domain);

		if (report == null) {
			report = new MatrixReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			String messageType = message.getType();
			if (messageType.equals("URL") || messageType.equals("Service")) {
				Matrix matrix = report.findOrCreateMatrix(message.getName());
				matrix.setType(message.getType());
				matrix.setName(message.getName());
				long duration = ((Transaction) message).getDurationInMicros();
				matrix.incCount();
				matrix.setTotalTime(matrix.getTotalTime() + duration);

				Map<String, Ratio> ratios = new HashMap<String, Ratio>();
				ratios.put("Call", new Ratio());
				ratios.put("SQL", new Ratio());
				ratios.put("Cache", new Ratio());

				processTransaction(tree, (Transaction) message, ratios);

				for (Entry<String, Ratio> entry : ratios.entrySet()) {
					String type = entry.getKey();
					Ratio ratio = entry.getValue();
					int count = ratio.getTotalCount();
					long time = ratio.getTotalTime();

					Ratio real = matrix.findOrCreateRatio(type);
					if (real.getMin() > count || real.getMin() == 0) {
						real.setMin(count);
					}
					if (real.getMax() < count) {
						real.setMax(count);
					}
					real.setTotalCount(real.getTotalCount() + count);
					real.setTotalTime(real.getTotalTime() + time);
				}
				// the message is required by some matrixs
				if (matrix.getUrl() == null) {
					matrix.setUrl(tree.getMessageId());
					storeMessage(tree);
				}
			}
		}
	}

	private void processTransaction(MessageTree tree, Transaction t, Map<String, Ratio> ratios) {
		List<Message> children = t.getChildren();
		String type = t.getType();
		Ratio ratio = null;
		if (type.equals("Call")) {
			ratio = ratios.get("Call");
		} else if (type.equals("SQL")) {
			ratio = ratios.get("SQL");
		} else if (type.startsWith("Cache.")) {
			ratio = ratios.get("Cache");
		}
		if (ratio != null) {
			ratio.incTotalCount();
			ratio.setTotalTime(ratio.getTotalTime() + t.getDurationInMicros());
		}

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(tree, (Transaction) child, ratios);
			}
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	private void storeMessage(MessageTree tree) {
		String messageId = tree.getMessageId();
		String domain = tree.getDomain();

		try {
			Bucket<MessageTree> logviewBucket = m_bucketManager.getLogviewBucket(m_startTime, domain);

			logviewBucket.storeById(messageId, tree);
		} catch (IOException e) {
			m_logger.error("Error when storing logview for matrix analyzer!", e);
		}
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "matrix");

			for (MatrixReport report : m_reports.values()) {
				Set<String> domainNames = report.getDomainNames();
				domainNames.clear();
				domainNames.addAll(getDomains());

				String xml = builder.buildXml(report);
				String domain = report.getDomain();

				reportBucket.storeById(domain, xml);
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (MatrixReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName("matrix");
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);

						// Task task = m_taskDao.createLocal();
						// task.setCreationDate(new Date());
						// task.setProducer(ip);
						// task.setReportDomain(domain);
						// task.setReportName("matrix");
						// task.setReportPeriod(period);
						// task.setStatus(1); // status todo
						// m_taskDao.insert(task);
						// m_logger.info("insert matrix task:" + task.toString());
					} catch (Throwable e) {
						Cat.getProducer().logError(e);
					}
				}
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing matrix reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}
}
