package com.dianping.cat.report.page.problem.transform;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;

public class HourlyLineChartVisitor extends BaseVisitor {

	private String m_ip;

	private String m_type;

	private String m_state;

	private LineChart m_graphItem = new LineChart();

	private Date m_start;

	private Map<Integer, Integer> m_value = new LinkedHashMap<Integer, Integer>();

	private static final int SIZE = 60;

	public HourlyLineChartVisitor(String ip, String type, String state, Date start) {
		m_ip = ip;
		m_type = type;
		m_state = state;
		m_start = start;

		m_graphItem.setSize(SIZE);
		m_graphItem.setStep(TimeHelper.ONE_MINUTE);
		m_graphItem.setStart(start);
	}

	private String buildSubTitle() {
		String subTitle = m_type;
		if (!StringUtils.isEmpty(m_state)) {
			subTitle += ":" + m_state;
		}
		return subTitle;
	}

	public LineChart getGraphItem() {
		Double[] value = new Double[SIZE];
		long minute = (System.currentTimeMillis()) / 1000 / 60 % 60;
		long current = System.currentTimeMillis();
		current -= current % Constants.HOUR;
		long size = (int) minute + 1;

		if (m_start.getTime() < current) {
			size = SIZE;
		}

		for (int i = 0; i < size; i++) {
			value[i] = 0.0;
		}

		for (int i = 0; i < SIZE; i++) {
			Integer temp = m_value.get(i);

			if (temp != null) {
				value[i] = temp.doubleValue();
			}
		}
		m_graphItem.add(buildSubTitle(), value);
		return m_graphItem;
	}

	@Override
	public void visitEntity(Entity entity) {
		String type = entity.getType();
		String state = entity.getStatus();

		if (m_state == null) {
			if (type.equals(m_type)) {
				super.visitEntity(entity);
			}
		} else {
			if (type.equals(m_type) && state.equals(m_state)) {
				super.visitEntity(entity);
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
