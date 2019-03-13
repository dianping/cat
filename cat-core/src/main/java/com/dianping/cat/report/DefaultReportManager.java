/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

import static com.dianping.cat.Constants.HOUR;

/**
	* Hourly report manager by domain of one report type(such as Transaction, Event, Problem, Heartbeat etc.) produced in one machine
	* for a couple of hours.
	*/
public class DefaultReportManager<T> extends ContainerHolder implements ReportManager<T>, Initializable, LogEnabled {
	@Inject
	private ReportDelegate<T> m_reportDelegate;

	@Inject
	private ReportBucketManager m_bucketManager;

	@Inject
	private HourlyReportDao m_reportDao;

	@Inject
	private HourlyReportContentDao m_reportContentDao;

	@Inject
	private DomainValidator m_validator;

	private String m_name;

	private Map<Long, Map<String, T>> m_reports = new ConcurrentHashMap<Long, Map<String, T>>();

	private Logger m_logger;

	public void cleanup(long time) {
		List<Long> startTimes = new ArrayList<Long>(m_reports.keySet());

		for (long startTime : startTimes) {
			if (startTime <= time) {
				synchronized (m_reports) {
					m_reports.remove(startTime);
				}
			}
		}
	}

	public void destory() {
		super.release(this);
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
			Set<String> domains = reports.keySet();
			Set<String> result = new HashSet<String>();

			for (String domain : domains) {
				if (m_validator.validate(domain)) {
					result.add(domain);
				}
			}
			return result;
		}
	}

	@Override
	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
		Map<String, T> reports = m_reports.get(startTime);

		if (reports == null && createIfNotExist) {
			synchronized (m_reports) {
				reports = m_reports.get(startTime);

				if (reports == null) {
					reports = new ConcurrentHashMap<String, T>();
					m_reports.put(startTime, reports);
				}
			}
		}

		if (reports == null) {
			reports = new LinkedHashMap<String, T>();
		}

		T report = reports.get(domain);

		if (report == null && createIfNotExist) {
			synchronized (reports) {
				report = m_reportDelegate.makeReport(domain, startTime, HOUR);
				reports.put(domain, report);
			}
		}

		if (report == null) {
			report = m_reportDelegate.makeReport(domain, startTime, HOUR);
		}

		return report;
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

	@Override
	public void initialize() {
	}

	@Override
	public Map<String, T> loadHourlyReports(long startTime, StoragePolicy policy, int index) {
		Transaction t = Cat.newTransaction("Restore", m_name);
		Map<String, T> reports = m_reports.get(startTime);
		Cat.logEvent("Restore", m_name + ":" + index);
		ReportBucket bucket = null;

		if (reports == null) {
			reports = new ConcurrentHashMap<String, T>();
			m_reports.put(startTime, reports);
		}

		try {
			bucket = m_bucketManager.getReportBucket(startTime, m_name, index);

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

	@Override
	public Map<String, T> loadLocalReports(long startTime, int index) {
		Transaction t = Cat.newTransaction("ReloadLocalTask", m_name);
		Cat.logEvent("ReloadLocal", m_name + ":" + index + ":" + new Date(startTime));
		ReportBucket bucket = null;
		Map<String, T> reports = new ConcurrentHashMap<String, T>();

		try {
			bucket = m_bucketManager.getReportBucket(startTime, m_name, index);

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				T report = m_reportDelegate.parseXml(xml);

				reports.put(id, report);
			}

			t.setStatus(Message.SUCCESS);
		} catch (Throwable e) {
			t.setStatus(e);
			Cat.logError(e);
		} finally {
			t.complete();

			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
		return reports;
	}

	public void setBucketManager(ReportBucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setReportContentDao(HourlyReportContentDao reportContentDao) {
		m_reportContentDao = reportContentDao;
	}

	public void setReportDao(HourlyReportDao reportDao) {
		m_reportDao = reportDao;
	}

	public void setReportDelegate(ReportDelegate<T> reportDelegate) {
		m_reportDelegate = reportDelegate;
	}

	public void setValidator(DomainValidator validator) {
		m_validator = validator;
	}

	private void storeDatabase(long startTime, Map<String, T> reports) {
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

				m_reportDao.insert(r);

				int id = r.getId();
				byte[] binaryContent = m_reportDelegate.buildBinary(report);
				HourlyReportContent content = m_reportContentDao.createLocal();

				content.setReportId(id);
				content.setContent(binaryContent);
				content.setPeriod(period);
				m_reportContentDao.insert(content);
				m_reportDelegate.createHourlyTask(report);
			} catch (Throwable e) {
				Cat.getProducer().logError(e);
			}
		}
	}

	private void storeFile(Map<String, T> reports, ReportBucket bucket) {
		for (T report : reports.values()) {
			try {
				String domain = m_reportDelegate.getDomain(report);
				String xml = m_reportDelegate.buildXml(report);

				bucket.storeById(domain, xml);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public void storeHourlyReports(long startTime, StoragePolicy policy, int index) {
		Transaction t = Cat.newTransaction("Checkpoint", m_name);
		Map<String, T> reports = m_reports.get(startTime);
		ReportBucket bucket = null;

		try {
			t.addData("reports", reports == null ? 0 : reports.size());

			if (reports != null) {
				Set<String> errorDomains = new HashSet<String>();

				for (String domain : reports.keySet()) {
					if (!m_validator.validate(domain)) {
						errorDomains.add(domain);
					}
				}
				for (String domain : errorDomains) {
					reports.remove(domain);
				}
				if (!errorDomains.isEmpty()) {
					m_logger.info("error domain:" + errorDomains);
				}

				m_reportDelegate.beforeSave(reports);

				if (policy.forFile()) {
					bucket = m_bucketManager.getReportBucket(startTime, m_name, index);

					try {
						storeFile(reports, bucket);
					} finally {
						m_bucketManager.closeBucket(bucket);
					}
				}

				if (policy.forDatabase()) {
					storeDatabase(startTime, reports);
				}
			}
			t.setStatus(Message.SUCCESS);
		} catch (Throwable e) {
			Cat.logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing %s reports of %s!", m_name, new Date(startTime)), e);
		} finally {
			cleanup(startTime);
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

}
