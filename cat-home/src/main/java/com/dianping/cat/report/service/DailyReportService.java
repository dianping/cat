package com.dianping.cat.report.service;

import java.util.Date;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.home.bug.entity.BugReport;

public interface DailyReportService {

	public CrossReport queryCrossReport(String domain, Date start, Date end);

	public EventReport queryEventReport(String domain, Date start, Date end);

	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end);

	public MatrixReport queryMatrixReport(String domain, Date start, Date end);

	public ProblemReport queryProblemReport(String domain, Date start, Date end);

	public SqlReport querySqlReport(String domain, Date start, Date end);

	public StateReport queryStateReport(String domain,Date start,Date end);
	
	public TransactionReport queryTransactionReport(String domain, Date start, Date end);

	public boolean insert(DailyReport report);

	public BugReport queryBugReport(String domain, Date start, Date end);
}
