package com.dianping.cat.report.page.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hsqldb.lib.StringUtil;
import org.unidal.web.mvc.view.annotation.EntityMeta;

import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.app.graph.AppSpeedDetail;
import com.dianping.cat.report.page.app.graph.AppSpeedDisplayInfo;
import com.dianping.cat.report.page.app.graph.PieChartDetailInfo;
import com.dianping.cat.report.page.app.processor.CrashLogProcessor.FieldsInfo;
import com.dianping.cat.service.app.command.AppDataSpreadInfo;
import com.site.lookup.util.StringUtils;

public class Model extends AbstractReportModel<Action, Context> {

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

	private List<AppDataSpreadInfo> m_appDataSpreadInfos;

	private AppSpeedDisplayInfo m_appSpeedDisplayInfo;

	private Map<Integer, Speed> m_speeds;

	private String m_content;

	private String m_fetchData;

	private int m_commandId;

	private ProblemStatistics m_problemStatistics;

	private FieldsInfo m_fieldsInfo;

	private ProblemReport m_problemReport;

	public Model(Context ctx) {
		super(ctx);
	}

	public List<AppDataSpreadInfo> getAppDataSpreadInfos() {
		return m_appDataSpreadInfos;
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

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public String getCommand() {
		Map<Integer, List<Code>> maps = new LinkedHashMap<Integer, List<Code>>();

		for (Command item : m_commands) {
			List<Code> items = maps.get(item.getId());

			if (items == null) {
				items = new ArrayList<Code>();
				maps.put(item.getId(), items);
			}
			items.addAll(item.getCodes().values());
		}
		return new JsonBuilder().toJson(maps);
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

	public String getDomainToCommandsJson() {
		Map<String, List<Command>> map = new LinkedHashMap<String, List<Command>>();

		for (Command command : m_commands) {
			String domain = command.getDomain();
			if (StringUtil.isEmpty(domain)) {
				domain = "default";
			}
			List<Command> commands = map.get(domain);

			if (commands == null) {
				commands = new ArrayList<Command>();
				map.put(domain, commands);
			}
			commands.add(command);
		}
		return new JsonBuilder().toJson(map);
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
		Map<String, List<Speed>> page2Steps = new LinkedHashMap<String, List<Speed>>();

		for (Speed speed : m_speeds.values()) {
			String page = speed.getPage();
			if (StringUtils.isEmpty(page)) {
				page = "default";
			}
			List<Speed> steps = page2Steps.get(page);
			if (steps == null) {
				steps = new ArrayList<Speed>();
				page2Steps.put(page, steps);
			}
			steps.add(speed);
		}
		return new JsonBuilder().toJson(page2Steps);
	}

	public Set<String> getPages() {
		Set<String> pages = new HashSet<String>();

		for (Speed speed : m_speeds.values()) {
			pages.add(speed.getPage());
		}
		return pages;
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

	public Map<Integer, Speed> getSpeeds() {
		return m_speeds;
	}

	public Map<Integer, Item> getVersions() {
		return m_versions;
	}

	public void setAppDataSpreadInfos(List<AppDataSpreadInfo> appDatas) {
		m_appDataSpreadInfos = appDatas;
	}

	public void setAppSpeedDisplayInfo(AppSpeedDisplayInfo appSpeedDisplayInfo) {
		m_appSpeedDisplayInfo = appSpeedDisplayInfo;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
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

	public void setSpeeds(Map<Integer, Speed> speeds) {
		m_speeds = speeds;
	}

	public void setVersions(Map<Integer, Item> versions) {
		m_versions = versions;
	}

}
