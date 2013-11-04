package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

@ModelMeta(ProblemAnalyzer.ID)
public class Model extends AbstractReportModel<Action, Context> {
	
	@EntityMeta
	private ProblemStatistics m_allStatistics;

	private int m_currentMinute; // for navigation

	private String m_defaultSqlThreshold;

	private String m_defaultThreshold;

	@EntityMeta
	private DetailStatistics m_detailStatistics;

	private String m_errorsTrend;

	@EntityMeta
	private GroupLevelInfo m_groupLevelInfo;

	private String m_groupName;

	private int m_hour;

	private int m_lastMinute; // last minute of current hour

	@EntityMeta
	private ProblemReport m_report;

	private String m_threadId;

	@EntityMeta
	private ThreadLevelInfo m_threadLevelInfo;

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

	public String getDefaultSqlThreshold() {
		return m_defaultSqlThreshold;
	}

	public String getDefaultThreshold() {
		return m_defaultThreshold;
	}

	public DetailStatistics getDetailStatistics() {
		return m_detailStatistics;
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
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return StringSortHelper.sortDomain(domainNames);
		}
	}

	public String getErrorsTrend() {
		return m_errorsTrend;
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

	public ProblemReport getReport() {
		return m_report;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public ThreadLevelInfo getThreadLevelInfo() {
		return m_threadLevelInfo;
	}

	public void setAllStatistics(ProblemStatistics allStatistics) {
		m_allStatistics = allStatistics;
	}

	public void setCurrentMinute(int currentMinute) {
		m_currentMinute = currentMinute;
	}

	public void setDefaultSqlThreshold(String defaultSqlThreshold) {
		m_defaultSqlThreshold = defaultSqlThreshold;
	}

	public void setDefaultThreshold(String defaultThreshold) {
		m_defaultThreshold = defaultThreshold;
	}

	public void setDetailStatistics(DetailStatistics detailStatistics) {
		m_detailStatistics = detailStatistics;
	}

	public void setErrorsTrend(String errorsTrend) {
		m_errorsTrend = errorsTrend;
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

	public void setReport(ProblemReport report) {
		m_report = report;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public void setThreadLevelInfo(ThreadLevelInfo threadLevelInfo) {
		m_threadLevelInfo = threadLevelInfo;
	}

}
