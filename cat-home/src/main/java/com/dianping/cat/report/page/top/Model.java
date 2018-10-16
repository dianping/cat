package com.dianping.cat.report.page.top;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.dependency.TopMetric;

@ModelMeta(TopAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	public String m_message;

	private int m_minute;

	private List<Integer> m_minutes;

	private int m_maxMinute;

	@EntityMeta
	private TopReport m_topReport;

	@EntityMeta
	private TopReport m_lastTopReport;

	private Date m_reportStart;

	private Date m_reportEnd;

	private TopMetric m_topMetric;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public TopReport getLastTopReport() {
		return m_lastTopReport;
	}

	public void setLastTopReport(TopReport lastTopReport) {
		m_lastTopReport = lastTopReport;
	}

	public int getMaxMinute() {
		return m_maxMinute;
	}

	public void setMaxMinute(int maxMinute) {
		m_maxMinute = maxMinute;
	}

	public String getMessage() {
		return m_message;
	}

	public void setMessage(String message) {
		m_message = message;
	}

	public int getMinute() {
		return m_minute;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public List<Integer> getMinutes() {
		return m_minutes;
	}

	public void setMinutes(List<Integer> minutes) {
		m_minutes = minutes;
	}

	public Date getReportEnd() {
		return m_reportEnd;
	}

	public void setReportEnd(Date reportEnd) {
		m_reportEnd = reportEnd;
	}

	public Date getReportStart() {
		return m_reportStart;
	}

	public void setReportStart(Date reportStart) {
		m_reportStart = reportStart;
	}

	public TopMetric getTopMetric() {
		return m_topMetric;
	}

	public void setTopMetric(TopMetric topMetric) {
		m_topMetric = topMetric;
	}

	public TopReport getTopReport() {
		return m_topReport;
	}

	public void setTopReport(TopReport topReport) {
		m_topReport = topReport;
	}

}
