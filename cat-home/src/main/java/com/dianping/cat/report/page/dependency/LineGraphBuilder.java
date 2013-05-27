package com.dianping.cat.report.page.dependency;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.LineChart;

public class LineGraphBuilder extends BaseVisitor {

	public Map<String, Map<String, Item>> m_dependencies = new ConcurrentHashMap<String, Map<String, Item>>();

	private static final String TOTAL_COUNT = "TotalCount";

	private static final String ERROR_COUNT = "ErrorCount";

	private static final String AVG = "Avg";

	private Set<String> m_types = new TreeSet<String>();

	private int m_currentMinute;

	private Date m_start;

	public Item findOrCreateItem(String type, String id) {
		Map<String, Item> items = m_dependencies.get(type);

		if (items == null) {
			items = new HashMap<String, Item>();
			m_dependencies.put(type, items);
		}

		Item result = items.get(id);

		if (result == null) {
			result = new Item();
			items.put(id, result);
		}

		return result;
	}

	public Map<String, List<LineChart>> queryDependencyGraph() {
		Map<String, List<LineChart>> allCharts = new HashMap<String, List<LineChart>>();
		for (String type : m_types) {
			List<LineChart> charts = new ArrayList<LineChart>();
			Map<String, Item> totalItems = m_dependencies.get(type + ':' + TOTAL_COUNT);
			Map<String, Item> errorItems = m_dependencies.get(type + ':' + ERROR_COUNT);
			Map<String, Item> avgItems = m_dependencies.get(type + ':' + AVG);

			charts.add(buildLineChart("Total", totalItems));
			charts.add(buildLineChart("Error", errorItems));
			charts.add(buildLineChart("Avg", avgItems));
			allCharts.put(type, charts);
		}
		return allCharts;
	}

	public List<LineChart> queryIndex() {
		List<LineChart> charts = new ArrayList<LineChart>();

		charts.add(buildLineChart("Total", m_dependencies.get(TOTAL_COUNT)));
		charts.add(buildLineChart("Error", m_dependencies.get(ERROR_COUNT)));
		charts.add(buildLineChart("Avg", m_dependencies.get(AVG)));
		return charts;
	}

	private LineChart buildLineChart(String title, Map<String, Item> items) {
		LineChart result = new LineChart();
		result.setSize(60);
		result.setStep(TimeUtil.ONE_MINUTE);
		result.setTitles(title);
		result.setStart(m_start);
		for (Entry<String, Item> entry : items.entrySet()) {
			String subTitle = entry.getKey();
			Item item = entry.getValue();

			result.addSubTitle(subTitle);
			result.addValue(item.getValue());
		}
		return result;
	}

	@Override
	public void visitDependency(Dependency dependency) {
		String type = dependency.getType();
		String target = dependency.getTarget();
		long count = dependency.getTotalCount();
		long error = dependency.getErrorCount();
		double avg = dependency.getAvg();

		m_types.add(type);
		findOrCreateItem(type + ':' + TOTAL_COUNT, target).setValue(m_currentMinute, count);
		findOrCreateItem(type + ':' + ERROR_COUNT, target).setValue(m_currentMinute, error);
		findOrCreateItem(type + ':' + AVG, target).setValue(m_currentMinute, avg / 1000);
		super.visitDependency(dependency);
	}

	@Override
	public void visitDependencyReport(DependencyReport dependencyReport) {
		m_start = dependencyReport.getStartTime();
		super.visitDependencyReport(dependencyReport);
	}

	@Override
	public void visitIndex(Index index) {
		String id = index.getName();
		long count = index.getTotalCount();
		long error = index.getErrorCount();
		double avg = index.getAvg();

		findOrCreateItem(TOTAL_COUNT, id).setValue(m_currentMinute, count);
		findOrCreateItem(ERROR_COUNT, id).setValue(m_currentMinute, error);
		findOrCreateItem(AVG, id).setValue(m_currentMinute, avg / 1000);
		super.visitIndex(index);
	}

	@Override
	public void visitSegment(Segment segment) {
		long count = segment.getExceptionCount();

		m_currentMinute = segment.getId();
		Item item = findOrCreateItem("count", "exception");

		item.setValue(m_currentMinute, count);
		super.visitSegment(segment);
	}

	public class Item {
		private double[] m_values = new double[60];

		private Item setValue(int minute, double value) {
			m_values[minute] = value;
			return this;
		}

		public double[] getValue() {
			return m_values;
		}
	}

}
