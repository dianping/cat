package com.dianping.cat.report.service.impl;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.home.dal.report.Monthreport;
import com.dianping.cat.home.dal.report.MonthreportDao;
import com.dianping.cat.home.dal.report.MonthreportEntity;
import com.dianping.cat.report.service.MonthReportService;

public class MonthReportServiceImpl implements MonthReportService {

	@Inject
	private MonthreportDao m_monthreportDao;

	@Override
	public CrossReport queryCrossReport(String domain, Date start) {
		try {
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "cross",
			      MonthreportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new CrossReport(domain);
	}

	@Override
	public DatabaseReport queryDatabaseReport(String database, Date start) {
		try {
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, database, "database",
			      MonthreportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.database.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new DatabaseReport(database);
	}

	@Override
	public EventReport queryEventReport(String domain, Date start) {
		try {
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "event",
			      MonthreportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new EventReport(domain);
	}

	@Override
	public HealthReport queryHealthReport(String domain, Date start) {
		try {
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "health",
			      MonthreportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.health.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HealthReport(domain);
	}

	@Override
	public HeartbeatReport queryHeartbeatReport(String domain, Date start) {
		try {
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "heartbeat",
			      MonthreportEntity.READSET_FULL);
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
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "matrix",
			      MonthreportEntity.READSET_FULL);
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
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "problem",
			      MonthreportEntity.READSET_FULL);
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
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "sql",
			      MonthreportEntity.READSET_FULL);
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
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "state",
			      MonthreportEntity.READSET_FULL);
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
			Monthreport entity = m_monthreportDao.findReportByDomainNamePeriod(start, domain, "transaction",
			      MonthreportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new TransactionReport(domain);
	}

}
