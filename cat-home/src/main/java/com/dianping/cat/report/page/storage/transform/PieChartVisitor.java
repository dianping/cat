package com.dianping.cat.report.page.storage.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.storage.model.entity.Machine;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.transform.BaseVisitor;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;

public class PieChartVisitor extends BaseVisitor {

	private String m_ip;

	private Map<String, Long> m_items = new HashMap<String, Long>();

	public String getPiechartJson() {
		if (m_items.size() > 0) {
			PieChart chart = new PieChart();
			List<Item> items = new ArrayList<Item>();

			for (Entry<String, Long> entry : m_items.entrySet()) {
				Item item = new Item();

				item.setNumber(entry.getValue()).setTitle(entry.getKey());
				items.add(item);
			}
			chart.addItems(items);
			return chart.getJsonString();
		} else {
			return null;
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		m_ip = machine.getId();

		super.visitMachine(machine);
	}

	@Override
	public void visitOperation(Operation operation) {
		long errors = operation.getError();

		if (errors > 0) {
			Long item = m_items.get(m_ip);

			if (item == null) {
				m_items.put(m_ip, errors);
			} else {
				m_items.put(m_ip, item + errors);
			}
		}
	}
}
