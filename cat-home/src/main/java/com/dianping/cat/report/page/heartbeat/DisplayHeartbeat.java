package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.report.graph.AbstractGraphPayload;
import com.dianping.cat.report.graph.GraphBuilder;

public class DisplayHeartbeat {
	public List<Period> m_periods = new ArrayList<Period>();

	public double[] m_activeThreads = new double[60];

	public double[] m_daemonThreads = new double[60];

	public double[] m_totalThreads = new double[60];

	public double[] m_newThreads = new double[60];

	private GraphBuilder m_builder;

	public DisplayHeartbeat() {

	}

	public DisplayHeartbeat(GraphBuilder builder) {
		m_builder = builder;
	}

	public DisplayHeartbeat display(HeartbeatReport report,String ip) {
		if (report == null) {
			return this;
		}
		Machine machine = report.getMachines().get(ip);
		if(machine==null){
			return this;
		}
		
		List<Period> periods = machine.getPeriods();
		int size = periods.size();

		for (; size > 0; size--) {
			Period period = periods.get(size - 1);
			m_periods.add(period);
			int minute = period.getMinute();
			m_activeThreads[minute] = period.getThreadCount();
			m_daemonThreads[minute] = period.getDaemonCount();
			m_totalThreads[minute] = period.getTotalStartedCount();
		}
		for (int i = 1; i <= 59; i++) {
			double d = m_totalThreads[i] - m_totalThreads[i - 1];
			if(d<0){
				d = m_totalThreads[i];
			}
			m_newThreads[i] = d;
		}
		return this;
	}

	public List<Period> getPeriods() {
		return m_periods;
	}

	public String getActiceThreadGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Active Thread", "Minute", "Count", m_activeThreads));
	}

	public String getDeamonThreadGraph() {
		return m_builder.build(new HeartbeatPayload(1, "Daemon Thread", "Minute", "Count", m_daemonThreads));
	}

	public String getTotalThreadGraph() {
		return m_builder.build(new HeartbeatPayload(2, "Total Started Thread", "Minute", "Count", m_totalThreads));
	}

	public String getStartedThreadGraph() {
		return m_builder.build(new HeartbeatPayload(3, "Started Thread", "Minute", "Count", m_newThreads));
	}

	public static class HeartbeatPayload extends AbstractGraphPayload {
		private String[] m_labels;

		private double[] m_values;

		private int m_index;

		private String m_idPrefix;

		public HeartbeatPayload(int index, String title, String axisXLabel, String axisYLabel, double[] values) {
			super(title, axisXLabel, axisYLabel);

			m_idPrefix = title.substring(0, 1);
			m_index = index;
			m_labels = new String[61];

			for (int i = 0; i <= 60; i++) {
				m_labels[i] = String.valueOf(i % 60);
			}
			m_values = values;
		}

		@Override
		public String getIdPrefix() {
			return m_idPrefix;
		}

		@Override
		public String getAxisXLabel(int index) {
			if (index % 5 == 0) {
				return m_labels[index];
			}
			return "";
		}

		@Override
		public int getOffsetX() {
			if (m_index % 2 == 1) {
				return getDisplayWidth();
			} else {
				return 0;
			}
		}

		@Override
		public int getOffsetY() {
			if (m_index / 2 == 1) {
				return getDisplayHeight() + 20;
			} else {
				return 0;
			}
		}

		@Override
		protected double[] loadValues() {
			return m_values;
		}

		@Override
		public int getDisplayHeight() {
			return (int) (super.getDisplayHeight() * 0.7);
		}

		@Override
		public int getDisplayWidth() {
			return (int) (super.getDisplayWidth() * 0.7);
		}

		@Override
		public int getWidth() {
			return super.getWidth() + 120;
		}

		@Override
		public boolean isStandalone() {
			return false;
		}
	}
}
