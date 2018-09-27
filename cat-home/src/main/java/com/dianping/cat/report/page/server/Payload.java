package com.dianping.cat.report.page.server;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.server.service.GraphParam;
import com.dianping.cat.server.MetricType;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("screen")
	private String m_screen;

	@FieldMeta("graph")
	private String m_graph;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("graphId")
	private long m_graphId;

	@FieldMeta("interval")
	private String m_interval;

	@FieldMeta("keywords")
	private String m_keywords;

	@FieldMeta("endPoints")
	private List<String> m_endPoints;

	@FieldMeta("endPoint")
	private String m_endPoint;

	@FieldMeta("measurements")
	private List<String> m_measurements;

	@FieldMeta("view")
	private String m_view = Constants.END_POINT;

	@FieldMeta("search")
	private String m_search = Constants.END_POINT;

	@FieldMeta("type")
	private String m_type = MetricType.AVG.getName();

	@ObjectMeta("graphParam")
	private GraphParam m_graphParam = new GraphParam();

	@FieldMeta("category")
	private String m_category;

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("ruleId")
	private int m_ruleId;

	@ObjectMeta("rule")
	private ServerAlarmRule m_rule;

	private ReportPage m_page;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.SERVER);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getCategory() {
		return m_category;
	}

	public String getContent() {
		return m_content;
	}

	public String getEndPoint() {
		return m_endPoint;
	}

	public List<String> getEndPoints() {
		return m_endPoints;
	}

	public String getGraph() {
		return m_graph;
	}

	public long getGraphId() {
		return m_graphId;
	}

	public GraphParam getGraphParam() {
		return m_graphParam;
	}

	public List<String> getGraphs() {
		String[] graphs = m_graph.split(",[ ]*");

		return Arrays.asList(graphs);
	}

	public String getGroup() {
		return m_group;
	}

	public Date getHistoryEndDate() {

		try {
			if (m_customEnd != null && m_customEnd.length() > 0) {
				return m_format.parse(m_customEnd);
			} else {
				return new Date();
			}
		} catch (Exception e) {
			return new Date();
		}
	}

	public Date getHistoryStartDate() {
		try {
			if (m_customStart != null && m_customStart.length() > 0) {

				return m_format.parse(m_customStart);
			} else {
				return new Date(System.currentTimeMillis() - TimeHelper.ONE_HOUR);
			}
		} catch (Exception e) {
			return new Date(System.currentTimeMillis() - TimeHelper.ONE_HOUR);
		}
	}

	public String getInterval() {
		return m_interval;
	}

	public String getKeywords() {
		return m_keywords;
	}

	public List<String> getKeywordsList() {
		if (m_keywords != null) {
			String[] keywordArray = m_keywords.split(" +");

			return Arrays.asList(keywordArray);
		} else {
			return Collections.emptyList();
		}
	}

	public List<String> getMeasurements() {
		return m_measurements;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public ServerAlarmRule getRule() {
		return m_rule;
	}

	public int getRuleId() {
		return m_ruleId;
	}

	public String getScreen() {
		return m_screen;
	}

	public String getSearch() {
		return m_search;
	}

	public String getType() {
		return m_type;
	}

	public String getView() {
		return m_view;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.SCREEN);
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setEndPoint(String endPoint) {
		m_endPoint = endPoint;
	}

	public void setEndPoints(String endPoints) {
		String[] ends = endPoints.split(",[ ]*");
		m_endPoints = Arrays.asList(ends);
	}

	public void setGraph(String graph) {
		m_graph = graph;
	}

	public void setGraphId(long graphId) {
		m_graphId = graphId;
	}

	public void setGraphParam(GraphParam graphParam) {
		m_graphParam = graphParam;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public void setInterval(String interval) {
		m_interval = interval;
	}

	public void setKeywords(String keywords) {
		m_keywords = keywords;
	}

	public void setMeasurements(String measurements) {
		String[] measures = measurements.split(",[ ]*");
		m_measurements = Arrays.asList(measures);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.SERVER);
	}

	public void setRule(ServerAlarmRule rule) {
		m_rule = rule;
	}

	public void setRuleId(int ruleId) {
		m_ruleId = ruleId;
	}

	public void setScreen(String screen) {
		m_screen = screen;
	}

	public void setSearch(String search) {
		m_search = search;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setView(String view) {
		m_view = view;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.SCREEN;
		}
	}
}
