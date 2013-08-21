package com.dianping.cat.report.service;

import java.util.Date;
import java.util.Set;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.service.entity.ServiceReport;

public interface HourlyReportService {
	public boolean insert(HourlyReport report);

	public Set<String> queryAllDomainNames(Date start, Date end, String reportName);

	public BugReport queryBugReport(String domain, Date start, Date end);

	public CrossReport queryCrossReport(String domain, Date start, Date end);

	public DependencyReport queryDependencyReport(String domain, Date start, Date end);

	public EventReport queryEventReport(String domain, Date start, Date end);

	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end);

	public MatrixReport queryMatrixReport(String domain, Date start, Date end);
	
	public MetricReport queryMetricReport(String group, Date start, Date end);
	
	public ProblemReport queryProblemReport(String domain, Date start, Date end);

	public ServiceReport queryServiceReport(String domain, Date start, Date end);

	public SqlReport querySqlReport(String domain, Date start, Date end);

	public StateReport queryStateReport(String domain,Date start,Date end);
	
	public TopReport queryTopReport(String domain,Date start,Date end);

	public TransactionReport queryTransactionReport(String domain, Date start, Date end);
}
