package com.dianping.cat.report.page.problem;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.LineChart;

public class HourlyLineChartVisitor extends BaseVisitor {

	private String m_ip;

	private String m_type;

	private String m_state;

	private LineChart m_graphItem = new LineChart();

	private Map<Integer, Integer> m_value = new LinkedHashMap<Integer, Integer>();

	private static final int SIZE = 60;

	public HourlyLineChartVisitor(String ip, String type, String state, Date start) {
		m_ip = ip;
		m_type = type;
		m_state = state;

		m_graphItem.setSize(SIZE);
		m_graphItem.setStep(TimeUtil.ONE_MINUTE);
		m_graphItem.setStart(start);

		String subTitle = type;
		if (!StringUtils.isEmpty(state)) {
			subTitle = type + ":" + state;
		}
		m_graphItem.addSubTitle(subTitle);
	}

	public LineChart getGraphItem() {
		double[] value = new double[SIZE];

		for (int i = 0; i < SIZE; i++) {
			Integer temp = m_value.get(i);

			if (temp != null) {
				value[i] = temp;
			}
		}
		m_graphItem.addValue(value);
		return m_graphItem;
	}

	@Override
	public void visitEntry(Entry entry) {
		String type = entry.getType();
		String state = entry.getStatus();

		if (m_state == null) {
			if (type.equals(m_type)) {
				super.visitEntry(entry);
			}
		} else {
			if (type.equals(m_type) && state.equals(m_state)) {
				super.visitEntry(entry);
			}
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		if (Constants.ALL.equals(m_ip) || m_ip.equals(machine.getIp())) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		int minute = segment.getId();
		int count = segment.getCount();

		Integer temp = m_value.get(minute);
		if (temp == null) {
			m_value.put(minute, count);
		} else {
			m_value.put(minute, count + temp);
		}
	}

}
