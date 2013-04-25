package com.dianping.cat.report;

import static com.dianping.cat.report.ReportConstants.HOUR;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dainping.cat.consumer.core.dal.Task;
import com.dainping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.TransactionReportTypeAggregator;
import com.dianping.cat.consumer.transaction.TransactionReportUrlFilter;
import com.dianping.cat.consumer.transaction.TransactionStatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.model.ModelPeriod;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class DefaultReportManager<T> implements ReportManager<T>, LogEnabled {
	public static final String ID = "transaction";

	@Inject(ID)
	private ReportService<T> m_reportService;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private Map<Long, Map<String, T>> m_map = new HashMap<Long, Map<String, T>>();

	private Logger m_logger;

	private TransactionReport buildAll(long startTime, Map<String, TransactionReport> reports) {
		TransactionReport all = createReport(ALL, startTime);
		TransactionReportTypeAggregator visitor = new TransactionReportTypeAggregator(all);

		try {
			for (TransactionReport report : reports.values()) {
				String domain = report.getDomain();

				all.getIps().add(domain);
				all.getDomainNames().add(domain);

				visitor.visitTransactionReport(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return all;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public T getAggregatedHourlyReport(long startTime, String domain) throws Exception {
		T report = m_reportService.createReport(ID, domain, startTime, HOUR);
		ModelPeriod period = ModelPeriod.getByTime(startTime);

		switch (period) {
		case HISTORICAL:
			return m_reportService.getHouylyReport(ID, domain, startTime);
		case LAST:
		case CURRENT:

			break;
		default:
			break;
		}

		return report;
	}

	@Override
	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
		Map<String, T> reports = m_map.get(startTime);

		if (reports == null && createIfNotExist) {
			synchronized (m_map) {
				reports = m_map.get(startTime);

				if (reports == null) {
					reports = new HashMap<String, T>();
					m_map.put(startTime, reports);
				}
			}
		}

		T report = reports == null ? null : reports.get(domain);

		if (report == null && createIfNotExist) {
			synchronized (reports) {
				report = m_reportService.createReport(ID, domain, startTime, HOUR);
				reports.put(domain, report);
			}
		}

		if (report == null) {
			return m_reportService.createReport(ID, domain, startTime, HOUR);
		} else {
			// report.getDomainNames().addAll(reports.keySet());
			// report.accept(new TransactionStatisticsComputer());

			return report;
		}
	}

	@Override
	public void storeReports(long startTime, FlushPolicy policy) {
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", ID);
		Map<String, T> reports = m_map.get(startTime);
		Bucket<String> bucket = null;

		try {
			if (policy.forFile()) {
				if (reports != null) {
					bucket = m_bucketManager.getReportBucket(startTime, ID);

					for (T report : reports.values()) {
						try {
							Set<String> domainNames = report.getDomainNames();

							domainNames.clear();
							domainNames.addAll(reports.keySet());

							report.accept(new TransactionStatisticsComputer());

							String xml = new TransactionReportUrlFilter().buildXml(report);
							String domain = report.getDomain();

							bucket.storeById(domain, xml);
						} catch (Exception e) {
							t.setStatus(e);
							Cat.getProducer().logError(e);
						}
					}
				}
			}

			if (policy.forDatabase()) {
				Date period = new Date(startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				TransactionReport all = buildAll(startTime, reports);

				reports.put(ALL, all);

				for (TransactionReport report : reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = new TransactionReportUrlFilter().buildXml(report);
						String domain = report.getDomain();

						r.setName("transaction");
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);

						Task task = m_taskDao.createLocal();
						task.setCreationDate(new Date());
						task.setProducer(ip);
						task.setReportDomain(domain);
						task.setReportName("transaction");
						task.setReportPeriod(period);
						task.setStatus(1); // status todo
						m_taskDao.insert(task);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.getProducer().logError(e);
					}
				}
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing transaction reports of %s!", new Date(startTime)), e);
		} finally {
			t.complete();

			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static enum FlushPolicy {
		FILE,

		FILE_AND_DB;

		public boolean forDatabase() {
			return this == FILE_AND_DB;
		}

		public boolean forFile() {
			return this == FILE_AND_DB || this == FILE;
		}
	}
}
