package com.dianping.cat.hadoop.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.LocalIP;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.storage.Bucket;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class RemoteStringBucket implements Bucket<String>, LogEnabled {
	@Inject
	private ReportDao m_reportDao;

	private Date m_period;

	private Logger m_logger;

	private String m_name;

	@Override
	public void close() throws IOException {
	}

	@Override
	public void deleteAndCreate() throws IOException {
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String findById(String domain) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> findAllById(String domain) throws IOException {
		try {
			List<Report> reports = m_reportDao.findByPeriodDomainTypeName(m_period, domain, 1, m_name, ReportEntity.READSET_FULL);
			List<String> contents = new ArrayList<String>(reports.size());
			for (Report r : reports) {
				contents.add(r.getContent());
			}
			return contents;
		} catch (DalException e) {
			throw new IOException(String.format("Unable to insert report(name=%s, domain=%s)!", m_name, domain), e);
		}
	}

	@Override
	public String findNextById(String id, String tag) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String findPreviousById(String id, String tag) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public Collection<String> getIdsByPrefix(String name) {
		try {
			List<Report> reports = m_reportDao.findAllByPeriodTypeName(m_period, 1, name, ReportEntity.READSET_FULL);
			List<String> ids = new ArrayList<String>(reports.size());

			for (Report report : reports) {
				ids.add(name + "-" + report.getDomain());
			}

			return ids;
		} catch (DalException e) {
			m_logger.error(String.format("Unable to get ids by prefix(%s)!", name), e);
		}

		return Collections.emptyList();
	}

	@Override
	public void initialize(Class<?> type, String name, Date timestamp) throws IOException {
		m_period = timestamp;
		m_name = name;
	}

	@Override
	public boolean storeById(String domain, String data) throws IOException {
		Transaction t = Cat.getProducer().newTransaction("Bucket", getClass().getSimpleName());
		Report report = m_reportDao.createLocal();

		report.setName(m_name);
		report.setDomain(domain);
		report.setType(1);
		report.setContent(data);
		report.setPeriod(m_period);
		report.setIp(LocalIP.getAddress());

		t.setStatus(Message.SUCCESS);

		try {
			m_reportDao.insert(report);

			return true;
		} catch (DalException e) {
			t.setStatus(e);
			throw new IOException(String.format("Unable to insert report(%s)!", domain), e);
		} finally {
			t.complete();
		}
	}
}
