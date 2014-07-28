package com.dianping.cat.report.service;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContent;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.nettopo.entity.NetGraphSet;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DefaultReportServiceManager extends ContainerHolder implements ReportServiceManager, Initializable {

	@Inject
	private HourlyReportDao m_hourlyReportDao;

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Inject
	private HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	private DailyReportContentDao m_dailyReportContentDao;

	@Inject
	private WeeklyReportContentDao m_weeklyReportContentDao;

	@Inject
	private MonthlyReportContentDao m_monthlyReportContentDao;

	private Map<String, ReportService> m_reportServices;

	@Override
	public void initialize() throws InitializationException {
		m_reportServices = lookupMap(ReportService.class);
	}

	@Override
	public boolean insertDailyReport(DailyReport report, byte[] content) {
		try {
			report.setContent("");
			m_dailyReportDao.insert(report);

			int id = report.getId();
			DailyReportContent proto = m_dailyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			m_dailyReportContentDao.insert(proto);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertHourlyReport(HourlyReport report, byte[] content) {
		try {
			report.setContent("");
			m_hourlyReportDao.insert(report);

			int id = report.getId();
			HourlyReportContent proto = m_hourlyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			m_hourlyReportContentDao.insert(proto);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertMonthlyReport(MonthlyReport report, byte[] content) {
		try {
			MonthlyReport monthReport = m_monthlyReportDao.findReportByDomainNamePeriod(report.getPeriod(),
			      report.getDomain(), report.getName(), MonthlyReportEntity.READSET_FULL);

			if (monthReport != null) {
				MonthlyReportContent reportContent = m_monthlyReportContentDao.createLocal();

				reportContent.setKeyReportId(monthReport.getId());
				reportContent.setReportId(monthReport.getId());
				m_monthlyReportDao.deleteReportByDomainNamePeriod(report);
				m_monthlyReportContentDao.deleteByPK(reportContent);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		try {
			m_monthlyReportDao.insert(report);

			int id = report.getId();
			MonthlyReportContent proto = m_monthlyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			m_monthlyReportContentDao.insert(proto);

			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean insertWeeklyReport(WeeklyReport report, byte[] content) {
		try {
			WeeklyReport weeklyReport = m_weeklyReportDao.findReportByDomainNamePeriod(report.getPeriod(),
			      report.getDomain(), report.getName(), WeeklyReportEntity.READSET_FULL);

			if (weeklyReport != null) {
				WeeklyReportContent reportContent = m_weeklyReportContentDao.createLocal();

				reportContent.setKeyReportId(weeklyReport.getId());
				reportContent.setReportId(weeklyReport.getId());
				m_weeklyReportContentDao.deleteByPK(reportContent);
				m_weeklyReportDao.deleteReportByDomainNamePeriod(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		try {
			m_weeklyReportDao.insert(report);

			int id = report.getId();
			WeeklyReportContent proto = m_weeklyReportContentDao.createLocal();

			proto.setReportId(id);
			proto.setContent(content);
			m_weeklyReportContentDao.insert(proto);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public AlertReport queryAlertReport(String domain, Date start, Date end) {
		ReportService<AlertReport> reportService = m_reportServices.get(Constants.REPORT_ALERT);

		return reportService.queryReport(domain, start, end);
	}

	public Set<String> queryAllDomainNames(Date start, Date end, String name) {
		ReportService<TransactionReport> reportService = m_reportServices.get(TransactionAnalyzer.ID);

		return reportService.queryAllDomainNames(start, end, name);
	}

	public BugReport queryBugReport(String domain, Date start, Date end) {
		ReportService<BugReport> reportService = m_reportServices.get(Constants.REPORT_BUG);

		return reportService.queryReport(domain, start, end);
	}

	public CrossReport queryCrossReport(String domain, Date start, Date end) {
		ReportService<CrossReport> reportService = m_reportServices.get(CrossAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	public DependencyReport queryDependencyReport(String domain, Date start, Date end) {
		ReportService<DependencyReport> reportService = m_reportServices.get(DependencyAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	public EventReport queryEventReport(String domain, Date start, Date end) {
		ReportService<EventReport> reportService = m_reportServices.get(EventAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end) {
		ReportService<HeartbeatReport> reportService = m_reportServices.get(HeartbeatAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	public HeavyReport queryHeavyReport(String domain, Date start, Date end) {
		ReportService<HeavyReport> reportService = m_reportServices.get(Constants.REPORT_HEAVY);

		return reportService.queryReport(domain, start, end);
	}

	public MatrixReport queryMatrixReport(String domain, Date start, Date end) {
		ReportService<MatrixReport> reportService = m_reportServices.get(MatrixAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	public MetricReport queryMetricReport(String domain, Date start, Date end) {
		ReportService<MetricReport> reportService = m_reportServices.get(MetricAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	@Override
	public NetGraphSet queryNetTopologyReport(String domain, Date start, Date end) {
		ReportService<NetGraphSet> reportService = m_reportServices.get(Constants.REPORT_NET_TOPOLOGY);

		return reportService.queryReport(domain, start, end);
	}

	public ProblemReport queryProblemReport(String domain, Date start, Date end) {
		ReportService<ProblemReport> reportService = m_reportServices.get(ProblemAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	@Override
   public RouterConfig queryRouterConfigReport(String domain, Date start, Date end) {
		ReportService<RouterConfig> reportService = m_reportServices.get(Constants.REPORT_ROUTER);

		return reportService.queryReport(domain, start, end);
   }

	public ServiceReport queryServiceReport(String domain, Date start, Date end) {
		ReportService<ServiceReport> reportService = m_reportServices.get(Constants.REPORT_SERVICE);

		return reportService.queryReport(domain, start, end);
	}

	public StateReport queryStateReport(String domain, Date start, Date end) {
		ReportService<StateReport> reportService = m_reportServices.get(StateAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	public TopReport queryTopReport(String domain, Date start, Date end) {
		ReportService<TopReport> reportService = m_reportServices.get(TopAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	public TransactionReport queryTransactionReport(String domain, Date start, Date end) {
		ReportService<TransactionReport> reportService = m_reportServices.get(TransactionAnalyzer.ID);

		return reportService.queryReport(domain, start, end);
	}

	@Override
	public UtilizationReport queryUtilizationReport(String domain, Date start, Date end) {
		ReportService<UtilizationReport> reportService = m_reportServices.get(Constants.REPORT_UTILIZATION);

		return reportService.queryReport(domain, start, end);
	}

}
