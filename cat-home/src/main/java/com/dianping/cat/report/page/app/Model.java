package com.dianping.cat.report.page.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.tuple.Pair;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppSpeedDetail;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;
import com.dianping.cat.report.page.app.display.DisplayCommands;
import com.dianping.cat.report.page.app.display.PieChartDetailInfo;
import com.dianping.cat.report.page.app.processor.CrashLogProcessor.FieldsInfo;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;

@ModelMeta(Constants.APP)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private LineChart m_lineChart;

	@EntityMeta
	private PieChart m_pieChart;

	private List<PieChartDetailInfo> m_pieChartDetailInfos;

	private Map<Integer, Item> m_cities;

	private Map<Integer, Item> m_versions;

	private Map<Integer, Item> m_connectionTypes;

	private Map<Integer, Item> m_operators;

	private Map<Integer, Item> m_networks;

	private Map<Integer, Item> m_platforms;

	private List<Command> m_commands;

	private List<AppDataDetail> m_appDataDetailInfos;

	private AppSpeedDisplayInfo m_appSpeedDisplayInfo;

	private String m_content;

	private String m_fetchData;

	private int m_commandId;

	private ProblemStatistics m_problemStatistics;

	private FieldsInfo m_fieldsInfo;

	private ProblemReport m_problemReport;

	private Map<String, List<Speed>> m_speeds;

	private Map<Integer, Code> m_codes;

	private List<String> m_codeDistributions;

	private Map<String, List<Command>> m_domain2Commands;

	private Map<Integer, List<Code>> m_command2Codes;

	private Map<String, Pair<String, String>> m_domain2Departments;

	@EntityMeta
	private AppReport m_appReport;

	private DisplayCommands m_displayCommands;

	public Model(Context ctx) {
		super(ctx);
	}

	public List<AppDataDetail> getAppDataDetailInfos() {
		return m_appDataDetailInfos;
	}

	public AppReport getAppReport() {
		return m_appReport;
	}

	public int getDefaultCommand() {
		return CommandQueryEntity.DEFAULT_COMMAND;
	}

	public int getDefaultActivity() {
		return CommandQueryEntity.DEFAULT_ACTIVITY;
	}

	public Map<String, Map<Integer, AppSpeedDetail>> getAppSpeedDetails() {
		Map<String, Map<Integer, AppSpeedDetail>> map = new LinkedHashMap<String, Map<Integer, AppSpeedDetail>>();
		Map<String, List<AppSpeedDetail>> details = m_appSpeedDisplayInfo.getAppSpeedDetails();

		if (details != null && !details.isEmpty()) {
			for (Entry<String, List<AppSpeedDetail>> entry : details.entrySet()) {
				Map<Integer, AppSpeedDetail> m = new LinkedHashMap<Integer, AppSpeedDetail>();

				for (AppSpeedDetail detail : entry.getValue()) {
					m.put(detail.getMinuteOrder(), detail);
				}
				map.put(entry.getKey(), m);
			}
		}
		return map;
	}

	public AppSpeedDisplayInfo getAppSpeedDisplayInfo() {
		return m_appSpeedDisplayInfo;
	}

	public Map<String, Map<Integer, AppSpeedDetail>> getAppSpeedSummarys() {
		Map<String, Map<Integer, AppSpeedDetail>> map = new LinkedHashMap<String, Map<Integer, AppSpeedDetail>>();
		Map<String, AppSpeedDetail> details = m_appSpeedDisplayInfo.getAppSpeedSummarys();

		if (details != null && !details.isEmpty()) {
			for (Entry<String, AppSpeedDetail> entry : details.entrySet()) {
				Map<Integer, AppSpeedDetail> m = new LinkedHashMap<Integer, AppSpeedDetail>();
				AppSpeedDetail d = entry.getValue();

				m.put(d.getMinuteOrder(), d);
				map.put(entry.getKey(), m);
			}
		}
		return map;
	}

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public List<String> getCodeDistributions() {
		return m_codeDistributions;
	}

	public Map<Integer, Code> getCodes() {
		return m_codes;
	}

	public Map<Integer, List<Code>> getCommand2Codes() {
		return m_command2Codes;
	}

	public String getCommand2CodesJson() {
		return new JsonBuilder().toJson(m_command2Codes);
	}

	public int getCommandId() {
		return m_commandId;
	}

	public List<Command> getCommands() {
		return m_commands;
	}

	public Map<Integer, Item> getConnectionTypes() {
		return m_connectionTypes;
	}

	public String getContent() {
		return m_content;
	}

	@Override
	public Action getDefaultAction() {
		return Action.LINECHART;
	}

	public DisplayCommands getDisplayCommands() {
		return m_displayCommands;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	public Map<String, List<Command>> getDomain2Commands() {
		return m_domain2Commands;
	}

	public String getDomain2CommandsJson() {
		Map<String, List<Command>> results = new LinkedHashMap<String, List<Command>>();

		results.put(Constants.ALL, m_commands);
		results.putAll(m_domain2Commands);
		return new JsonBuilder().toJson(results);
	}

	public Map<String, Pair<String, String>> getDomain2Departments() {
		return m_domain2Departments;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public String getFetchData() {
		return m_fetchData;
	}

	public FieldsInfo getFieldsInfo() {
		return m_fieldsInfo;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public Map<Integer, Item> getNetworks() {
		return m_networks;
	}

	public Map<Integer, Item> getOperators() {
		return m_operators;
	}

	public String getPage2StepsJson() {
		return new JsonBuilder().toJson(m_speeds);
	}

	public Set<String> getPages() {
		return m_speeds.keySet();
	}

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public List<PieChartDetailInfo> getPieChartDetailInfos() {
		return m_pieChartDetailInfos;
	}

	public Map<Integer, Item> getPlatforms() {
		return m_platforms;
	}

	public ProblemReport getProblemReport() {
		return m_problemReport;
	}

	public ProblemStatistics getProblemStatistics() {
		return m_problemStatistics;
	}

	public Map<String, List<Speed>> getSpeeds() {
		return m_speeds;
	}

	public Map<Integer, Item> getVersions() {
		return m_versions;
	}

	public void setAppDataDetailInfos(List<AppDataDetail> appDataDetailInfos) {
		m_appDataDetailInfos = appDataDetailInfos;
	}

	public void setAppReport(AppReport appReport) {
		m_appReport = appReport;
	}

	public void setAppSpeedDisplayInfo(AppSpeedDisplayInfo appSpeedDisplayInfo) {
		m_appSpeedDisplayInfo = appSpeedDisplayInfo;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
	}

	public void setCodeDistributions(List<String> codeDistributions) {
		m_codeDistributions = codeDistributions;
	}

	public void setCodes(Map<Integer, Code> codes) {
		m_codes = codes;
	}

	public void setCommand2Codes(Map<Integer, List<Code>> command2Codes) {
		m_command2Codes = command2Codes;
	}

	public void setCommandId(int commandId) {
		m_commandId = commandId;
	}

	public void setCommands(List<Command> commands) {
		m_commands = commands;
	}

	public void setConnectionTypes(Map<Integer, Item> map) {
		m_connectionTypes = map;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDisplayCommands(DisplayCommands displayCommands) {
		m_displayCommands = displayCommands;
	}

	public void setDomain2Commands(Map<String, List<Command>> domain2Commands) {
		m_domain2Commands = domain2Commands;
	}

	public void setDomain2Departments(Map<String, Pair<String, String>> domain2Departments) {
		m_domain2Departments = domain2Departments;
	}

	public void setFetchData(String fetchData) {
		m_fetchData = fetchData;
	}

	public void setFieldsInfo(FieldsInfo fieldsInfo) {
		m_fieldsInfo = fieldsInfo;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public void setNetworks(Map<Integer, Item> networks) {
		m_networks = networks;
	}

	public void setOperators(Map<Integer, Item> operators) {
		m_operators = operators;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	public void setPieChartDetailInfos(List<PieChartDetailInfo> pieChartDetailInfos) {
		m_pieChartDetailInfos = pieChartDetailInfos;
	}

	public void setPlatforms(Map<Integer, Item> platforms) {
		m_platforms = platforms;
	}

	public void setProblemReport(ProblemReport problemReport) {
		m_problemReport = problemReport;
	}

	public void setProblemStatistics(ProblemStatistics problemStatistics) {
		m_problemStatistics = problemStatistics;
	}

	public void setSpeeds(Map<String, List<Speed>> speeds) {
		m_speeds = speeds;
	}

	public void setVersions(Map<Integer, Item> versions) {
		m_versions = versions;
	}

}
