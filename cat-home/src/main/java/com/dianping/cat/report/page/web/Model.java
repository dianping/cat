package com.dianping.cat.report.page.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.view.annotation.EntityMeta;

import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.app.display.PieChartDetailInfo;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private ProblemStatistics m_allStatistics;

	private Map<String, PatternItem> m_pattermItems;

	@EntityMeta
	private LineChart m_lineChart;

	@EntityMeta
	private PieChart m_pieChart;

	private List<PieChartDetailInfo> m_pieChartDetailInfos;

	private Date m_start;

	private Date m_end;

	private Date m_compareStart;

	private Date m_compareEnd;

	private String m_json;

	private ProblemReport m_problemReport;

	private Map<Integer, Item> m_cities;

	private Map<Integer, Item> m_operators;

	private Map<Integer, Code> m_codes;

	private String m_defaultApi;

	public Model(Context ctx) {
		super(ctx);
	}

	public ProblemStatistics getAllStatistics() {
		return m_allStatistics;
	}

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public Map<Integer, Code> getCodes() {
		return m_codes;
	}

	public Date getCompareEnd() {
		return m_compareEnd;
	}

	public Date getCompareStart() {
		return m_compareStart;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDefaultApi() {
		return m_defaultApi;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public Date getEnd() {
		return m_end;
	}

	public List<String> getIps() {
		if (m_problemReport == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_problemReport.getIps());
		}
	}

	public String getJson() {
		return m_json;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public Map<Integer, Item> getOperators() {
		return m_operators;
	}

	public Map<String, PatternItem> getPattermItems() {
		return m_pattermItems;
	}

	public String getPattern2Items() {
		return new JsonBuilder().toJson(m_pattermItems);
	}

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public List<PieChartDetailInfo> getPieChartDetailInfos() {
		return m_pieChartDetailInfos;
	}

	public ProblemReport getProblemReport() {
		return m_problemReport;
	}

	public Date getStart() {
		return m_start;
	}

	public void setAllStatistics(ProblemStatistics allStatistics) {
		m_allStatistics = allStatistics;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
	}

	public void setCodes(Map<Integer, Code> codes) {
		m_codes = codes;
	}

	public void setCompareEnd(Date compareEnd) {
		m_compareEnd = compareEnd;
	}

	public void setCompareStart(Date compareStart) {
		m_compareStart = compareStart;
	}

	public void setDefaultApi(String defaultApi) {
		m_defaultApi = defaultApi;
	}

	public void setEnd(Date end) {
		m_end = end;
	}

	public void setJson(String json) {
		m_json = json;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public void setOperators(Map<Integer, Item> operators) {
		m_operators = operators;
	}

	public void setPattermItems(Map<String, PatternItem> pattermItems) {
		m_pattermItems = pattermItems;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	public void setPieChartDetailInfos(List<PieChartDetailInfo> pieChartDetailInfos) {
		m_pieChartDetailInfos = pieChartDetailInfos;
	}

	public void setProblemReport(ProblemReport problemReport) {
		m_problemReport = problemReport;
	}

	public void setStart(Date start) {
		m_start = start;
	}

}
