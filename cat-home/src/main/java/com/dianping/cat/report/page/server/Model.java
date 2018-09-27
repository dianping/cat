package com.dianping.cat.report.page.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.server.entity.ServerMetricConfig;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.metric.Range;
import com.dianping.cat.report.page.server.display.MetricScreenInfo;
import com.dianping.cat.server.MetricType;

@ModelMeta("model")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_opState = SUCCESS;

	private String m_config;

	private Date m_startTime;

	private Date m_endTime;

	private Map<String, List<String>> m_endPoints;

	private Set<String> m_graphs;

	private String m_json;

	private MetricScreenInfo m_metricScreenInfo;

	private Map<String, Map<String, MetricScreenInfo>> m_metricScreenInfos;

	private ServerMetricConfig m_serverMetricConfig;

	private List<ServerAlarmRule> m_serverAlarmRules;

	private ServerAlarmRule m_serverAlarmRule;

	private String m_content;

	public Model(Context ctx) {
		super(ctx);
	}

	public Range[] getAllRange() {
		return Range.values();
	}

	public String getConfig() {
		return m_config;
	}

	public String getContent() {
		return m_content;
	}

	@Override
	public Action getDefaultAction() {
		return Action.GRAPH;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new HashSet<String>();
	}

	public Map<String, List<String>> getEndPoints() {
		return m_endPoints;
	}

	public String getEndPointsJson() {
		return new JsonBuilder().toJson(m_endPoints);
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public Set<String> getGraphs() {
		return m_graphs;
	}

	public String getJson() {
		return m_json;
	}

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public MetricScreenInfo getMetricScreenInfo() {
		return m_metricScreenInfo;
	}

	public Map<String, Map<String, MetricScreenInfo>> getMetricScreenInfos() {
		return m_metricScreenInfos;
	}

	public List<MetricType> getMetricTypes() {
		return Arrays.asList(MetricType.values());
	}

	public Map<String, String> getMetricTitles() {
		Map<String, String> titles = new HashMap<String, String>();

		for (MetricType type : MetricType.values()) {
			titles.put(type.getName(), type.getTitle());
		}
		return titles;
	}

	public String getOpState() {
		return m_opState;
	}

	public ServerAlarmRule getServerAlarmRule() {
		return m_serverAlarmRule;
	}

	public List<ServerAlarmRule> getServerAlarmRules() {
		return m_serverAlarmRules;
	}

	public ServerMetricConfig getServerMetricConfig() {
		return m_serverMetricConfig;
	}

	public String getServerMetricConfigJson() {
		return new JsonBuilder().toJson(m_serverMetricConfig.getGroups());
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public void setConfig(String config) {
		m_config = config;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setEndPoints(Map<String, List<String>> endPoints) {
		m_endPoints = endPoints;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

	public void setGraphs(Set<String> graphs) {
		m_graphs = graphs;
	}

	public void setJson(String json) {
		m_json = json;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public void setMetricScreenInfo(MetricScreenInfo metricScreenInfo) {
		m_metricScreenInfo = metricScreenInfo;
	}

	public void setMetricScreenInfos(Map<String, Map<String, MetricScreenInfo>> metricScreenInfos) {
		m_metricScreenInfos = metricScreenInfos;
	}

	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public void setOpState(String opState) {
		m_opState = opState;
	}

	public void setServerAlarmRule(ServerAlarmRule serverAlarmRule) {
		m_serverAlarmRule = serverAlarmRule;
	}

	public void setServerAlarmRules(List<ServerAlarmRule> serverAlarmRules) {
		m_serverAlarmRules = serverAlarmRules;
	}

	public void setServerMetricConfig(ServerMetricConfig serverMetricConfig) {
		m_serverMetricConfig = serverMetricConfig;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}
}
