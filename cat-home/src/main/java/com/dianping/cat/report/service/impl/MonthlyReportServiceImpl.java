package com.dianping.cat.report.service.impl;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.report.service.MonthlyReportService;

public class MonthlyReportServiceImpl implements MonthlyReportService {

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Override
	public CrossReport queryCrossReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "cross",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new CrossReport(domain);
	}

	@Override
	public EventReport queryEventReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "event",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new EventReport(domain);
	}

	@Override
	public HeartbeatReport queryHeartbeatReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "heartbeat",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HeartbeatReport(domain);
	}

	@Override
	public MatrixReport queryMatrixReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "matrix",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new MatrixReport(domain);
	}

	@Override
	public ProblemReport queryProblemReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "problem",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new ProblemReport(domain);
	}

	@Override
	public SqlReport querySqlReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "sql",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new SqlReport(domain);
	}

	@Override
	public StateReport queryStateReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "state",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.state.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new StateReport(domain);
	}

	@Override
	public TransactionReport queryTransactionReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "transaction",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new TransactionReport(domain);
	}

	@Override
	public boolean insert(MonthlyReport report) {
		try {
			m_monthlyReportDao.insert(report);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

}
