package com.dianping.cat.report.page.appstats.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.AppCommandGroupConfigManager;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.appstats.display.AppCommandsSorter;
import com.dianping.cat.report.page.appstats.display.CodeDisplayVisitor;
import com.dianping.cat.report.page.appstats.display.DisplayCode;
import com.dianping.cat.report.page.appstats.display.DisplayCommand;
import com.dianping.cat.report.page.appstats.display.DisplayCommands;

@Named
public class AppStatisticBuilder {

	@Inject
	private AppStatisticReportService m_appReportService;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	@Inject
	private AppCommandGroupConfigManager m_commandGroupConfigManager;

	public Set<String> buildCodeKeys(DisplayCommands displayCommands) {

		Set<String> ids = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int id1 = Integer.parseInt(o1.replaceAll("X", "0"));
				int id2 = Integer.parseInt(o2.replaceAll("X", "0"));

				return id2 - id1;
			}
		});

		for (DisplayCommand displaycmd : displayCommands.getCommands().values()) {
			for (String id : displaycmd.getCodes().keySet()) {
				if (id.contains("XX") || CodeDisplayVisitor.STANDALONES.contains(Integer.valueOf(id))) {
					ids.add(id);
				}
			}
		}
		return ids;
	}

	public Map<String, PieChart> buildCodePiecharts(List<String> codeKeys, DisplayCommands displayCommands, int top) {
		Set<Integer> groupIds = buildGroupIds();
		Map<String, PieChart> results = new LinkedHashMap<String, PieChart>();
		Map<String, List<DistributionPiechartData>> dists = new LinkedHashMap<String, List<DistributionPiechartData>>();

		for (Entry<Integer, DisplayCommand> entry : displayCommands.getCommands().entrySet()) {
			int commandId = entry.getKey();

			if (!groupIds.contains(commandId)) {
				for (Entry<String, DisplayCode> code : entry.getValue().getCodes().entrySet()) {
					String codeKey = code.getKey();

					if (codeKeys.contains(codeKey)) {
						List<DistributionPiechartData> datas = dists.get(codeKey);

						if (datas == null) {
							datas = new ArrayList<DistributionPiechartData>();

							dists.put(codeKey, datas);
						}
						datas.add(new DistributionPiechartData(commandId, code.getValue().getCount()));
					}
				}
			}
		}

		Map<String, List<DistributionPiechartData>> sorted = pruneDistributionDatas(dists, top);

		for (Entry<String, List<DistributionPiechartData>> entry : sorted.entrySet()) {
			results.put(entry.getKey(), buildPieChart(entry.getKey(), entry.getValue()));
		}

		results = SortHelper.sortMap(results, new Comparator<Entry<String, PieChart>>() {

			@Override
			public int compare(Entry<String, PieChart> o1, Entry<String, PieChart> o2) {
				int id1 = Integer.parseInt(o1.getKey().replaceAll("X", "0"));
				int id2 = Integer.parseInt(o2.getKey().replaceAll("X", "0"));

				return id2 - id1;
			}
		});
		return results;
	}

	public DisplayCommands buildDisplayCommands(AppReport report, String sort) throws IOException {
		CodeDisplayVisitor distributionVisitor = new CodeDisplayVisitor(m_appConfigManager);

		distributionVisitor.visitAppReport(report);
		DisplayCommands displayCommands = distributionVisitor.getCommands();

		AppCommandsSorter sorter = new AppCommandsSorter(displayCommands, sort);
		displayCommands = sorter.getSortedCommands();
		return displayCommands;
	}

	private Set<Integer> buildGroupIds() {
		Set<String> commands = m_commandGroupConfigManager.getConfig().getCommands().keySet();
		Set<Integer> ids = new HashSet<Integer>();

		for (String command : commands) {
			Command cmd = m_appConfigManager.getCommands().get(command);

			if (cmd != null) {
				ids.add(cmd.getId());
			}
		}
		return ids;
	}

	private PieChart buildPieChart(String title, List<DistributionPiechartData> datas) {
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();

		for (DistributionPiechartData data : datas) {
			Item item = new Item();
			int commandId = data.getCommand();
			Command command = m_appConfigManager.getRawCommands().get(commandId);
			String name = null;

			if (command != null) {
				name = command.getName();
			} else {
				name = "Unknown Command [" + commandId + "]";
			}
			item.setTitle(name);
			item.setId(data.getCommand());
			item.setNumber(data.getCount());
			items.add(item);
		}

		pieChart.setTitle(title);
		pieChart.addItems(items);
		return pieChart;
	}

	private Map<String, List<DistributionPiechartData>> pruneDistributionDatas(
	      Map<String, List<DistributionPiechartData>> dists, int max) {
		Map<String, List<DistributionPiechartData>> sorted = new LinkedHashMap<String, List<DistributionPiechartData>>();

		for (Entry<String, List<DistributionPiechartData>> entry : dists.entrySet()) {
			List<DistributionPiechartData> data = entry.getValue();

			Collections.sort(data, new Comparator<DistributionPiechartData>() {
				@Override
				public int compare(DistributionPiechartData o1, DistributionPiechartData o2) {
					if (o2.getCount() > o1.getCount()) {
						return 1;
					} else if (o2.getCount() < o1.getCount()) {
						return -1;
					} else {
						return 0;
					}
				}
			});
			int size = data.size();
			int index = size > max ? max : size;

			sorted.put(entry.getKey(), data.subList(0, index));
		}
		return sorted;
	}

	public AppReport queryAppReport(int namespace, Date startDate) {
		Date endDate = TimeHelper.addDays(startDate, 1);
		AppReport report = m_appReportService.queryDailyReport(namespace, startDate, endDate);

		return report;
	}

	public static class DistributionPiechartData {
		private int m_command;

		private long m_count;

		public DistributionPiechartData(int command, long count) {
			m_command = command;
			m_count = count;
		}

		public int getCommand() {
			return m_command;
		}

		public long getCount() {
			return m_count;
		}
	}

}
