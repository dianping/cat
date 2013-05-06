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
import com.dianping.cat.home.dal.report.Weeklyreport;
import com.dianping.cat.home.dal.report.WeeklyreportDao;
import com.dianping.cat.home.dal.report.WeeklyreportEntity;
import com.dianping.cat.report.service.WeeklyReportService;

public class WeeklyReportServiceImpl implements WeeklyReportService {

	@Inject
	private WeeklyreportDao m_weeklyreportDao;

	@Override
	public CrossReport queryCrossReport(String domain, Date start) {
		try {
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "cross",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, database, "database",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "event",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "health",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "heartbeat",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "matrix",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "problem",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "sql",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "state",
			      WeeklyreportEntity.READSET_FULL);
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
			Weeklyreport entity = m_weeklyreportDao.findReportByDomainNamePeriod(start, domain, "transaction",
			      WeeklyreportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new TransactionReport(domain);
	}

}
