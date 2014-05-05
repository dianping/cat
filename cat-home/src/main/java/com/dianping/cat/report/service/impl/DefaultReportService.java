package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
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
import com.dianping.cat.home.alertReport.entity.AlertReport;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.dal.report.DailyReportContent;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContent;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContent;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.report.service.ReportService;

public class DefaultReportService implements ReportService {

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

	@Inject
	private TransactionReportService m_transactionReportService;

	@Inject
	private EventReportService m_eventReportService;

	@Inject
	private ProblemReportService m_problemReportService;

	@Inject
	private HeartbeatReportService m_heartbeatReportService;

	@Inject
	private CrossReportService m_crossReportService;

	@Inject
	private MatrixReportService m_matrixReportService;

	@Inject
	private DependencyReportService m_dependencyReportService;

	@Inject
	private TopReportService m_topReportService;

	@Inject
	private BugReportService m_bugReportService;

	@Inject
	private HeavyReportService m_heavyReportService;
	
	@Inject 
	private AlertReportService m_alertReportService;

	@Inject
	private ServiceReportService m_serviceReportService;

	@Inject
	private StateReportService m_stateReportService;

	@Inject
	private MetricReportService m_metricReportService;

	@Inject
	private UtilizationReportService m_utilizationReportService;

	@Override
	public boolean insertDailyReport(DailyReport report, byte[] content) {
		try {
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
			MonthlyReportContent reportContent = m_monthlyReportContentDao.createLocal();

			reportContent.setKeyReportId(monthReport.getId());
			reportContent.setReportId(monthReport.getId());
			m_monthlyReportContentDao.deleteByPK(reportContent);
		} catch (Exception e) {
			Cat.logError(e);
		}

		try {
			m_monthlyReportDao.deleteReportByDomainNamePeriod(report);
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
			WeeklyReport monthReport = m_weeklyReportDao.findReportByDomainNamePeriod(report.getPeriod(),
			      report.getDomain(), report.getName(), WeeklyReportEntity.READSET_FULL);
			WeeklyReportContent reportContent = m_weeklyReportContentDao.createLocal();

			reportContent.setKeyReportId(monthReport.getId());
			reportContent.setReportId(monthReport.getId());
			m_weeklyReportContentDao.deleteByPK(reportContent);
		} catch (Exception e) {
			Cat.logError(e);
		}
		try {
			m_weeklyReportDao.deleteReportByDomainNamePeriod(report);
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

	public Set<String> queryAllDomainNames(Date start, Date end, String name) {
		return m_transactionReportService.queryAllDomainNames(start, end, name);
	}

	public BugReport queryBugReport(String domain, Date start, Date end) {
		return m_bugReportService.queryReport(domain, start, end);
	}

	public CrossReport queryCrossReport(String domain, Date start, Date end) {
		return m_crossReportService.queryReport(domain, start, end);
	}

	public DependencyReport queryDependencyReport(String domain, Date start, Date end) {
		return m_dependencyReportService.queryReport(domain, start, end);
	}

	public EventReport queryEventReport(String domain, Date start, Date end) {
		return m_eventReportService.queryReport(domain, start, end);
	}

	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end) {
		return m_heartbeatReportService.queryReport(domain, start, end);
	}

	public HeavyReport queryHeavyReport(String domain, Date start, Date end) {
		return m_heavyReportService.queryReport(domain, start, end);
	}

	public MatrixReport queryMatrixReport(String domain, Date start, Date end) {
		return m_matrixReportService.queryReport(domain, start, end);
	}

	public MetricReport queryMetricReport(String domain, Date start, Date end) {
		return m_metricReportService.queryReport(domain, start, end);
	}

	public ProblemReport queryProblemReport(String domain, Date start, Date end) {
		return m_problemReportService.queryReport(domain, start, end);
	}

	public ServiceReport queryServiceReport(String domain, Date start, Date end) {
		return m_serviceReportService.queryReport(domain, start, end);
	}

	public StateReport queryStateReport(String domain, Date start, Date end) {
		return m_stateReportService.queryReport(domain, start, end);
	}

	public TopReport queryTopReport(String domain, Date start, Date end) {
		return m_topReportService.queryReport(domain, start, end);
	}

	public TransactionReport queryTransactionReport(String domain, Date start, Date end) {
		return m_transactionReportService.queryReport(domain, start, end);
	}

	@Override
	public UtilizationReport queryUtilizationReport(String domain, Date start, Date end) {
		return m_utilizationReportService.queryReport(domain, start, end);
	}

	@Override
   public AlertReport queryAlertReport(String domain, Date start, Date end) {
	   return m_alertReportService.queryReport(domain, start, end);
   }

}
