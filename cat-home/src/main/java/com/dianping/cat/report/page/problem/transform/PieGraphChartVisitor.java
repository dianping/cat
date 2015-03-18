package com.dianping.cat.report.page.problem.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;

public class PieGraphChartVisitor extends BaseVisitor {

	private String m_type;

	private String m_status;

	private Map<String, Integer> m_items = new HashMap<String, Integer>();

	private String m_ip;

	public PieGraphChartVisitor(String type, String status) {
		m_type = type;
		m_status = status;
	}

	public PieChart getPieChart() {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (java.util.Map.Entry<String, Integer> entry : m_items.entrySet()) {
			Item item = new Item();

			item.setNumber(entry.getValue()).setTitle(entry.getKey());
			items.add(item);
		}
		chart.addItems(items);

		return chart;
	}

	@Override
	public void visitDuration(Duration duration) {
		int count = duration.getCount();
		Integer old = m_items.get(m_ip);

		if (old == null) {
			m_items.put(m_ip, count);
		} else {
			m_items.put(m_ip, count + old);
		}
	}

	@Override
	public void visitEntity(Entity entity) {
		String type = entity.getType();
		String name = entity.getStatus();

		if (type.equals(m_type)) {
			if (StringUtils.isEmpty(m_status) || name.equals(m_status)) {
				super.visitEntity(entity);
			}
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		m_ip = machine.getIp();

		super.visitMachine(machine);
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}

	@Override
	public void visitThread(JavaThread thread) {
		super.visitThread(thread);
	}

}
