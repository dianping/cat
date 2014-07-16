package com.dianping.cat.report.page.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.view.annotation.EntityMeta;

import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.LineChart;

public class Model extends AbstractReportModel<Action, Context> {

	@EntityMeta
	private LineChart m_lineChart;

	private List<Item> m_cities;

	private List<Item> m_versions;

	private List<Item> m_channels;

	private List<Item> m_operators;

	private List<Item> m_networks;

	private List<Item> m_platforms;

	private List<Command> m_commands;

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

	public List<Item> getPlatforms() {
		return m_platforms;
	}

	public void setPlatforms(List<Item> platforms) {
		m_platforms = platforms;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	public List<Item> getChannels() {
		return m_channels;
	}

	public List<Item> getCities() {
		return m_cities;
	}

	public List<Command> getCommands() {
		return m_commands;
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

	public List<Item> getNetworks() {
		return m_networks;
	}

	public List<Item> getOperators() {
		return m_operators;
	}

	public List<Item> getVersions() {
		return m_versions;
	}

	public void setChannels(List<Item> channels) {
		m_channels = channels;
	}

	public void setCities(List<Item> cities) {
		m_cities = cities;
	}

	public void setCommands(List<Command> commands) {
		m_commands = commands;
	}

	public void setNetworks(List<Item> networks) {
		m_networks = networks;
	}

	public void setOperators(List<Item> operators) {
		m_operators = operators;
	}

	public void setVersions(List<Item> versions) {
		m_versions = versions;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}
}
