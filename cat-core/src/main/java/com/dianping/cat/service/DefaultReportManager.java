package com.dianping.cat.service;

import static com.dianping.cat.Constants.HOUR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

/**
 * Hourly report manager by domain of one report type(such as Transaction, Event, Problem, Heartbeat etc.) produced in
 * one machine for a couple of hours.
 */
public class DefaultReportManager<T> implements ReportManager<T>, LogEnabled {
	@Inject
	private ReportDelegate<T> m_reportDelegate;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private HourlyReportDao m_reportDao;

	@Inject
	private HourlyReportContentDao m_reportContentDao;

	private String m_name;

	private Map<Long, Map<String, T>> m_reports = new ConcurrentHashMap<Long, Map<String, T>>();

	private Logger m_logger;

	@Override
	public void cleanup() {
		long currentStartTime = ModelPeriod.CURRENT.getStartTime();
		long threshold = currentStartTime - 2 * Constants.HOUR;
		List<Long> startTimes = new ArrayList<Long>(m_reports.keySet());

		for (long startTime : startTimes) {
			if (startTime <= threshold) {
				m_reports.remove(startTime); // too old to stay in memory
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains(long startTime) {
		Map<String, T> reports = m_reports.get(startTime);

		if (reports == null) {
			return new HashSet<String>();
		} else {
			return reports.keySet();
		}
	}

	@Override
	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
		if (createIfNotExist) {
			cleanup();
		}

		Map<String, T> reports = m_reports.get(startTime);

		if (reports == null && createIfNotExist) {
			synchronized (m_reports) {
				reports = m_reports.get(startTime);

				if (reports == null) {
					reports = new HashMap<String, T>();
					m_reports.put(startTime, reports);
				}
			}
		}

		T report = reports == null ? null : reports.get(domain);

		if (report == null && createIfNotExist) {
			synchronized (reports) {
				report = m_reportDelegate.makeReport(domain, startTime, HOUR);
				reports.put(domain, report);
			}
		}

		if (report == null) {
			return report = m_reportDelegate.makeReport(domain, startTime, HOUR);
		} else {
			return report;
		}
	}

	@Override
	public Map<String, T> getHourlyReports(long startTime) {
		Map<String, T> reports = m_reports.get(startTime);

		if (reports == null) {
			return Collections.emptyMap();
		} else {
			return reports;
		}
	}

	public void initialize() {
		long currentStartTime = ModelPeriod.CURRENT.getStartTime();

		loadHourlyReports(currentStartTime, StoragePolicy.FILE);
		loadHourlyReports(currentStartTime - Constants.HOUR, StoragePolicy.FILE);
	}

	@Override
	public Map<String, T> loadHourlyReports(long startTime, StoragePolicy policy) {
		Transaction t = Cat.newTransaction("Restore", m_name);
		Map<String, T> reports = m_reports.get(startTime);
		Bucket<String> bucket = null;

		if (reports == null) {
			reports = new HashMap<String, T>();
			m_reports.put(startTime, reports);
		}

		try {
			bucket = m_bucketManager.getReportBucket(startTime, m_name);

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				T report = m_reportDelegate.parseXml(xml);

				reports.put(id, report);
			}

			m_reportDelegate.afterLoad(reports);
			t.setStatus(Message.SUCCESS);
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.logError(e);
			m_logger.error(String.format("Error when loading %s reports of %s!", m_name, new Date(startTime)), e);
		} finally {
			t.complete();

			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
		return reports;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public void storeHourlyReports(long startTime, StoragePolicy policy) {
		Transaction t = Cat.newTransaction("Checkpoint", m_name);
		Map<String, T> reports = m_reports.get(startTime);
		Bucket<String> bucket = null;

		try {
			t.addData("reports", reports == null ? 0 : reports.size());

			if (reports != null) {
				m_reportDelegate.beforeSave(reports);

				if (policy.forFile()) {
					bucket = m_bucketManager.getReportBucket(startTime, m_name);

					try {
						for (T report : reports.values()) {
							try {
								String domain = m_reportDelegate.getDomain(report);
								String xml = m_reportDelegate.buildXml(report);

								bucket.storeById(domain, xml);
							} catch (Exception e) {
								t.setStatus(e);
								Cat.logError(e);
							}
						}
					} finally {
						bucket.close();
					}
				}

				if (policy.forDatabase()) {
					Date period = new Date(startTime);
					String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

					for (T report : reports.values()) {
						try {
							String domain = m_reportDelegate.getDomain(report);
							HourlyReport r = m_reportDao.createLocal();

							r.setName(m_name);
							r.setDomain(domain);
							r.setPeriod(period);
							r.setIp(ip);
							r.setType(1);
							r.setContent("");

							m_reportDao.insert(r);

							int id = r.getId();
							byte[] binaryContent = m_reportDelegate.buildBinary(report);
							HourlyReportContent content = m_reportContentDao.createLocal();

							content.setReportId(id);
							content.setContent(binaryContent);
							m_reportContentDao.insert(content);
							m_reportDelegate.createHourlyTask(report);
						} catch (Throwable e) {
							t.setStatus(e);
							Cat.getProducer().logError(e);
						}
					}
				}
			}
			t.setStatus(Message.SUCCESS);
		} catch (Throwable e) {
			Cat.logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing %s reports of %s!", m_name, new Date(startTime)), e);
		} finally {
			t.complete();

			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static enum StoragePolicy {
		FILE,

		FILE_AND_DB;

		public boolean forDatabase() {
			return this == FILE_AND_DB;
		}

		public boolean forFile() {
			return this == FILE_AND_DB || this == FILE;
		}
	}

	public void setReportDelegate(ReportDelegate<T> reportDelegate) {
   	m_reportDelegate = reportDelegate;
   }

	public void setBucketManager(BucketManager bucketManager) {
   	m_bucketManager = bucketManager;
   }

	public void setReportDao(HourlyReportDao reportDao) {
   	m_reportDao = reportDao;
   }

	public void setReportContentDao(HourlyReportContentDao reportContentDao) {
   	m_reportContentDao = reportContentDao;
   }

}
