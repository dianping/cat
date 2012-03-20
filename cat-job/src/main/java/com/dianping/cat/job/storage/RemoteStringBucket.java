package com.dianping.cat.job.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.job.sql.dal.Report;
import com.dianping.cat.job.sql.dal.ReportDao;
import com.dianping.cat.job.sql.dal.ReportEntity;
import com.dianping.cat.storage.Bucket;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class RemoteStringBucket implements Bucket<String>, LogEnabled {
	@Inject
	private ReportDao m_reportDao;

	private Date m_period;

	private Logger m_logger;

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
	public String findById(String id) throws IOException {
		int pos = id.indexOf('-');

		if (pos > 0) {
			String name = id.substring(0, pos);
			String domain = id.substring(pos + 1);

			try {
				Report report = m_reportDao
				      .findByPeriodDomainTypeName(m_period, domain, 1, name, ReportEntity.READSET_FULL);

				return report.getContent();
			} catch (DalException e) {
				throw new IOException(String.format("Unable to insert report(%s)!", id), e);
			}
		}

		return null;
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
	}

	@Override
	public boolean storeById(String id, String data) throws IOException {
		Report report = m_reportDao.createLocal();
		int pos = id.indexOf('-');

		if (pos > 0) {
			String name = id.substring(0, pos);
			String domain = id.substring(pos + 1);

			report.setName(name);
			report.setDomain(domain);
			report.setType(1);
			report.setContent(data);
			report.setPeriod(m_period);

			try {
				m_reportDao.insert(report);

				return true;
			} catch (DalException e) {
				throw new IOException(String.format("Unable to insert report(%s)!", id), e);
			}
		}

		return false;
	}
}
