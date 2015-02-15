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
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.highload.entity.HighloadReport;
import com.dianping.cat.home.jar.entity.JarReport;
import com.dianping.cat.home.network.entity.NetGraphSet;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.system.entity.SystemReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public interface ReportServiceManager {

	public boolean insertDailyReport(DailyReport report, byte[] content);

	public boolean insertHourlyReport(HourlyReport report, byte[] content);

	public boolean insertMonthlyReport(MonthlyReport report, byte[] content);

	public boolean insertWeeklyReport(WeeklyReport report, byte[] content);

	public Set<String> queryAllDomainNames(Date start, Date end, String reportName);

	public BugReport queryBugReport(String domain, Date start, Date end);

	public CrossReport queryCrossReport(String domain, Date start, Date end);

	public DependencyReport queryDependencyReport(String domain, Date start, Date end);

	public EventReport queryEventReport(String domain, Date start, Date end);

	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end);

	public HeavyReport queryHeavyReport(String domain, Date start, Date end);

	public AlertReport queryAlertReport(String domain, Date start, Date end);

	public MatrixReport queryMatrixReport(String domain, Date start, Date end);

	public MetricReport queryMetricReport(String domain, Date start, Date end);

	public ProblemReport queryProblemReport(String domain, Date start, Date end);

	public HighloadReport queryHighloadReport(String domain, Date start, Date end);

	public ServiceReport queryServiceReport(String domain, Date start, Date end);

	public StateReport queryStateReport(String domain, Date start, Date end);

	public TopReport queryTopReport(String domain, Date start, Date end);

	public TransactionReport queryTransactionReport(String domain, Date start, Date end);

	public UtilizationReport queryUtilizationReport(String domain, Date start, Date end);

	public NetGraphSet queryNetTopologyReport(String domain, Date start, Date end);

	public RouterConfig queryRouterConfigReport(String domain, Date start, Date end);

	public JarReport queryJarReport(String domain, Date start, Date end);

	public SystemReport querySystemReport(String domain, Date start, Date end);

	public StorageReport queryStorageReport(String domain, Date start, Date end);
}
