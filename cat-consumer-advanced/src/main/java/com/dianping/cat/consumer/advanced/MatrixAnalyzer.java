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
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.core.dal.Report;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.matrix.model.entity.Matrix;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.entity.Ratio;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.matrix.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class MatrixAnalyzer extends AbstractMessageAnalyzer<MatrixReport> implements LogEnabled {
	public static final String ID = "matrix";
	
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	private Map<String, MatrixReport> m_reports = new HashMap<String, MatrixReport>();

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public MatrixReport getReport(String domain) {
		MatrixReport report = m_reports.get(domain);

		if (report == null) {
			report = new MatrixReport(domain);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}

		report.getDomainNames().addAll(m_reports.keySet());
		return report;
	}

	@Override
	protected void loadReports() {
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
	public void process(MessageTree tree) {
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

			if (shouldDiscard((Transaction) message)) {
				return;
			}
			if (messageType.equals("URL") || messageType.equals("Service") || messageType.equals("PigeonService")) {
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
				}
			}
		}
	}

	private void processTransaction(MessageTree tree, Transaction t, Map<String, Ratio> ratios) {
		List<Message> children = t.getChildren();
		String type = t.getType();
		Ratio ratio = null;

		if (type.equals("Call") || type.equals("PigeonCall")) {
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

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "matrix");

			for (MatrixReport report : m_reports.values()) {
				try {
					try {
						report.accept(new MatrixReportFilter());
					} catch (Exception e) {
						// ConcurrentModificationException
						report.accept(new MatrixReportFilter());
					}
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(m_reports.keySet());

					String xml = null;
					try {
						xml = builder.buildXml(report);
					} catch (Exception e) {
						xml = builder.buildXml(report);
					}
					String domain = report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (MatrixReport report : m_reports.values()) {
					try {
						report.accept(new MatrixReportFilter());
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName(ID);
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.getProducer().logError(e);
					}
				}
			}
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
