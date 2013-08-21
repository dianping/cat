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
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.service.entity.ServiceReport;

public interface WeeklyReportService {

	public boolean insert(WeeklyReport report);

	public BugReport queryBugReport(String domain, Date start);

	public CrossReport queryCrossReport(String domain, Date start);

	public EventReport queryEventReport(String domain, Date start);

	public HeartbeatReport queryHeartbeatReport(String domain, Date start);

	public MatrixReport queryMatrixReport(String domain, Date start);

	public ProblemReport queryProblemReport(String domain, Date start);
	
	public ServiceReport queryServiceReport(String domain, Date start);

	public SqlReport querySqlReport(String domain, Date start);

	public StateReport queryStateReport(String domain, Date start);

	public TransactionReport queryTransactionReport(String domain, Date start);
}
