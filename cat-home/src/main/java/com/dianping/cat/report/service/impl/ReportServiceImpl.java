package com.dianping.cat.report.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

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
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.report.service.HourlyReportService;
import com.dianping.cat.report.service.MonthlyReportCache;
import com.dianping.cat.report.service.MonthlyReportService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.service.WeeklyReportCache;
import com.dianping.cat.report.service.WeeklyReportService;

public class ReportServiceImpl implements ReportService {
	@Inject
	private HourlyReportService m_hourlyReportService;

	@Inject
	private DailyReportService m_dailyReportService;

	@Inject
	private WeeklyReportService m_weeklyReportService;

	@Inject
	private MonthlyReportService m_monthlyReportService;

	@Inject
	private WeeklyReportCache m_weeklyReportCache;

	@Inject
	private MonthlyReportCache m_monthReportCache;

	public static final int s_hourly = 1;

	public static final int s_daily = 2;

	public static final int s_historyDaily = 3;

	public static final int s_currentWeekly = 4;

	public static final int s_historyWeekly = 5;

	public static final int s_currentMonth = 6;

	public static final int s_historyMonth = 7;

	public static final int s_customer = 8;

	public int getQueryType(Date start, Date end) {
		long endTime = end.getTime();
		long startTime = start.getTime();
		long currentWeek = TimeUtil.getCurrentWeek().getTime();
		long currentMonth = TimeUtil.getCurrentMonth().getTime();
		long duration = endTime - startTime;

		if (duration == TimeUtil.ONE_HOUR) {
			return s_hourly;
		}
		if (duration == TimeUtil.ONE_DAY) {
			return s_daily;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 7) {
			if (duration == TimeUtil.ONE_DAY * 7) {
				if (startTime == currentWeek) {
					return s_currentWeekly;
				} else {
					return s_historyWeekly;
				}
			}
		}

		cal = Calendar.getInstance();
		cal.setTime(start);
		if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
			cal.setTime(end);
			if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
				if (startTime == currentMonth) {
					return s_currentMonth;
				} else {
					return s_historyMonth;
				}
			}
		}
		return s_customer;
	}

	@Override
   public boolean insertDailyReport(DailyReport report) {
		return m_dailyReportService.insert(report);
   }

	@Override
   public boolean insertHourlyReport(HourlyReport report) {
		return m_hourlyReportService.insert(report);
   }

	@Override
   public boolean insertMonthlyReport(MonthlyReport report) {
		return m_monthlyReportService.insert(report);
   }

	@Override
   public boolean insertWeeklyReport(WeeklyReport report) {
		return m_weeklyReportService.insert(report);
   }

	@Override
	public Set<String> queryAllDomainNames(Date start, Date end, String reportName) {
		return m_hourlyReportService.queryAllDomainNames(start, end, reportName);
	}

	@Override
	public BugReport queryBugReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		BugReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryBugReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryBugReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryBugReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryBugReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryBugReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryBugReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryBugReport(domain, start);
		} else {
			report = m_dailyReportService.queryBugReport(domain, start, end);
		}
		if (report == null) {
			report = new BugReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

	@Override
	public CrossReport queryCrossReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		CrossReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryCrossReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryCrossReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryCrossReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryCrossReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryCrossReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryCrossReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryCrossReport(domain, start);
		} else {
			report = m_dailyReportService.queryCrossReport(domain, start, end);
		}
		if (report == null) {
			report = new CrossReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

	@Override
	public DependencyReport queryDependencyReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		
		if (type == s_hourly) {
			return m_hourlyReportService.queryDependencyReport(domain, start, end);
		} else {
			throw new RuntimeException("Top report don't have other report type but houly!");
		}
	}

	@Override
	public EventReport queryEventReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		EventReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryEventReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryEventReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryEventReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryEventReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryEventReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryEventReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryEventReport(domain, start);
		} else {
			report = m_dailyReportService.queryEventReport(domain, start, end);
		}
		if (report == null) {
			report = new EventReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

	@Override
	public HeartbeatReport queryHeartbeatReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		HeartbeatReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryHeartbeatReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryHeartbeatReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryHeartbeatReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryHeartbeatReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryHeartbeatReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryHeartbeatReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryHeartbeatReport(domain, start);
		} else {
			report = m_dailyReportService.queryHeartbeatReport(domain, start, end);
		}
		if (report == null) {
			report = new HeartbeatReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}
	
	@Override
	public MatrixReport queryMatrixReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		MatrixReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryMatrixReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryMatrixReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryMatrixReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryMatrixReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryMatrixReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryMatrixReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryMatrixReport(domain, start);
		} else {
			report = m_dailyReportService.queryMatrixReport(domain, start, end);
		}
		if (report == null) {
			report = new MatrixReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

	@Override
	public MetricReport queryMetricReport(String group, Date start, Date end) {
		int type = getQueryType(start, end);
		
		if (type == s_hourly) {
			return m_hourlyReportService.queryMetricReport(group, start, end);
		} else {
			throw new RuntimeException("unexcepted query type in metric report!");
		}
	}
	
	@Override
	public ProblemReport queryProblemReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		ProblemReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryProblemReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryProblemReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryProblemReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryProblemReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryProblemReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryProblemReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryProblemReport(domain, start);
		} else {
			report = m_dailyReportService.queryProblemReport(domain, start, end);
		}
		if (report == null) {
			report = new ProblemReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}
	
	@Override
	public ServiceReport queryServiceReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		ServiceReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryServiceReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryServiceReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryServiceReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryServiceReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryServiceReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryServiceReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryServiceReport(domain, start);
		} else {
			report = m_dailyReportService.queryServiceReport(domain, start, end);
		}
		if (report == null) {
			report = new ServiceReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

	@Override
	public SqlReport querySqlReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		SqlReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.querySqlReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.querySqlReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.querySqlReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.querySqlReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.querySqlReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.querySqlReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.querySqlReport(domain, start);
		} else {
			report = m_dailyReportService.querySqlReport(domain, start, end);
		}
		if (report == null) {
			report = new SqlReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

	@Override
	public StateReport queryStateReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		StateReport report = null;
		
		if (type == s_hourly) {
			report = m_hourlyReportService.queryStateReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryStateReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryStateReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryStateReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryStateReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryStateReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryStateReport(domain, start);
		} else {
			report = m_dailyReportService.queryStateReport(domain, start, end);
		}
		if (report == null) {
			report = new StateReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

	@Override
	public TopReport queryTopReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		
		if (type == s_hourly) {
			return m_hourlyReportService.queryTopReport(domain, start, end);
		} else {
			throw new RuntimeException("Top report don't have other report type but houly!");
		}
	}

	@Override
	public TransactionReport queryTransactionReport(String domain, Date start, Date end) {
		int type = getQueryType(start, end);
		TransactionReport report = null;

		if (type == s_hourly) {
			report = m_hourlyReportService.queryTransactionReport(domain, start, end);
		} else if (type == s_daily) {
			report = m_dailyReportService.queryTransactionReport(domain, start, end);
		} else if (type == s_historyDaily) {
			report = m_dailyReportService.queryTransactionReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			report = m_weeklyReportService.queryTransactionReport(domain, start);
		} else if (type == s_currentWeekly) {
			report = m_weeklyReportCache.queryTransactionReport(domain, start);
		} else if (type == s_historyMonth) {
			report = m_monthlyReportService.queryTransactionReport(domain, start);
		} else if (type == s_currentMonth) {
			report = m_monthReportCache.queryTransactionReport(domain, start);
		} else {
			report = m_dailyReportService.queryTransactionReport(domain, start, end);
		}
		if (report == null) {
			report = new TransactionReport(domain);
			report.setStartTime(start).setEndTime(end);
		}
		return report;
	}

}
