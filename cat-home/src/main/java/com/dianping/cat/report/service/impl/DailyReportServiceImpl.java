package com.dianping.cat.report.service.impl;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.page.model.matrix.MatrixReportMerger;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.model.sql.SqlReportMerger;
import com.dianping.cat.report.page.model.state.StateReportMerger;
import com.dianping.cat.report.service.DailyReportService;

public class DailyReportServiceImpl implements DailyReportService {

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Override
	public CrossReport queryCrossReport(String domain, Date start, Date end) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "cross";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				CrossReport reportModel = com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		CrossReport crossReport = merger.getCrossReport();

		crossReport.setStartTime(start);
		crossReport.setEndTime(end);
		return crossReport;
	}

	@Override
	public EventReport queryEventReport(String domain, Date start, Date end) {
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));

		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "event";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				EventReport reportModel = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		EventReport eventReport = merger.getEventReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(end);
		return eventReport;
	}

	@Override
	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(domain));

		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "heartbeat";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				HeartbeatReport reportModel = com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser
				      .parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();

		heartbeatReport.setStartTime(start);
		heartbeatReport.setEndTime(end);
		return heartbeatReport;
	}

	@Override
	public MatrixReport queryMatrixReport(String domain, Date start, Date end) {
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "matrix";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				MatrixReport reportModel = com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		MatrixReport matrixReport = merger.getMatrixReport();

		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		return matrixReport;
	}

	@Override
	public ProblemReport queryProblemReport(String domain, Date start, Date end) {
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "problem";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				ProblemReport reportModel = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		ProblemReport problemReport = merger.getProblemReport();

		problemReport.setStartTime(start);
		problemReport.setEndTime(end);
		return problemReport;
	}

	@Override
	public SqlReport querySqlReport(String domain, Date start, Date end) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(domain));

		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "sql";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				SqlReport reportModel = com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser.parse(xml);

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		SqlReport sqlReport = merger.getSqlReport();

		sqlReport.setStartTime(start);
		sqlReport.setEndTime(end);
		return sqlReport;
	}

	@Override
	public StateReport queryStateReport(String domain, Date start, Date end) {
		StateReportMerger merger = new StateReportMerger(new StateReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "state";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				StateReport reportModel = com.dianping.cat.consumer.state.model.transform.DefaultSaxParser.parse(xml);

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		StateReport stateReport = merger.getStateReport();

		stateReport.setStartTime(start);
		stateReport.setEndTime(end);
		return stateReport;
	}

	@Override
	public TransactionReport queryTransactionReport(String domain, Date start, Date end) {
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "transaction";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				TransactionReport reportModel = com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser
				      .parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(start);
		transactionReport.setEndTime(end);
		return transactionReport;
	}

}
