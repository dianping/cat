package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private ProblemReport m_report;

	private int m_lastMinute; // last minute of current hour

	private int m_hour;

	private String m_threadId;

	private int m_currentMinute; // for navigation

	private int m_threshold;

	private int m_sqlThreshold;
	
	private String m_groupName;

	private String m_defaultThreshold;
	
	private String m_defaultSqlThreshold;

	private GroupLevelInfo m_groupLevelInfo;

	private ThreadLevelInfo m_threadLevelInfo;

	private ProblemStatistics m_problemStatistics;

	private ProblemStatistics m_allStatistics;
	
	private DetailStatistics m_detailStatistics;

	private String m_mobileResponse;

	private String m_errorsTrend;
	
	public Model(Context ctx) {
		super(ctx);
	}

	public ProblemStatistics getAllStatistics() {
		return m_allStatistics;
	}

	public int getCurrentMinute() {
		return m_currentMinute;
	}

	@Override
	public Action getDefaultAction() {
		return Action.GROUP;
	}

	public String getDefaultThreshold() {
		return m_defaultThreshold;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDomainNames());
		}
	}

	public GroupLevelInfo getGroupLevelInfo() {
		return m_groupLevelInfo;
	}

	public String getGroupName() {
		return m_groupName;
	}

	public int getHour() {
		return m_hour;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getIps());
		}
	}

	public int getLastMinute() {
		return m_lastMinute;
	}

	public int getMinuteLast() {
		if (m_currentMinute == 0) {
			return 0;
		}
		return m_currentMinute - 1;
	}

	public int getMinuteNext() {
		if (m_currentMinute == 59) {
			return 59;
		}
		return m_currentMinute + 1;
	}

	public String getMobileResponse() {
   	return m_mobileResponse;
   }

	public ProblemStatistics getProblemStatistics() {
		return m_problemStatistics;
	}

	public ProblemReport getReport() {
		return m_report;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public ThreadLevelInfo getThreadLevelInfo() {
		return m_threadLevelInfo;
	}

	public int getThreshold() {
		return m_threshold;
	}

	public void setAllStatistics(ProblemStatistics allStatistics) {
		m_allStatistics = allStatistics;
	}

	public void setCurrentMinute(int currentMinute) {
		m_currentMinute = currentMinute;
	}

	public void setDefaultThreshold(String defaultThreshold) {
		m_defaultThreshold = defaultThreshold;
	}

	public void setGroupLevelInfo(GroupLevelInfo groupLevelInfo) {
		m_groupLevelInfo = groupLevelInfo;
	}

	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public void setHour(int hour) {
		m_hour = hour;
	}

	public void setLastMinute(int lastMinute) {
		m_lastMinute = lastMinute;
	}

	public void setMobileResponse(String mobileResponse) {
   	m_mobileResponse = mobileResponse;
   }

	public void setProblemStatistics(ProblemStatistics problemStatistics) {
		m_problemStatistics = problemStatistics;
	}

	public void setReport(ProblemReport report) {
		m_report = report;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public void setThreadLevelInfo(ThreadLevelInfo threadLevelInfo) {
		m_threadLevelInfo = threadLevelInfo;
	}

	public void setThreshold(int threshold) {
		m_threshold = threshold;
	}

	public String getErrorsTrend() {
   	return m_errorsTrend;
   }

	public void setErrorsTrend(String errorsTrend) {
   	m_errorsTrend = errorsTrend;
   }

	public String getDefaultSqlThreshold() {
   	return m_defaultSqlThreshold;
   }

	public void setDefaultSqlThreshold(String defaultSqlThreshold) {
   	m_defaultSqlThreshold = defaultSqlThreshold;
   }

	public int getSqlThreshold() {
   	return m_sqlThreshold;
   }

	public void setSqlThreshold(int sqlThreshold) {
   	m_sqlThreshold = sqlThreshold;
   }

	public DetailStatistics getDetailStatistics() {
   	return m_detailStatistics;
   }

	public void setDetailStatistics(DetailStatistics detailStatistics) {
   	m_detailStatistics = detailStatistics;
   }
	
}
