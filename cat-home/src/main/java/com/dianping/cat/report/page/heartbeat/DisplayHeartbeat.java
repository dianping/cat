package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.report.graph.AbstractGraphPayload;
import com.dianping.cat.report.graph.GraphBuilder;

public class DisplayHeartbeat {
	public static final int SIZE = 1024;

	public List<Period> m_periods = new ArrayList<Period>();

	public double[] m_activeThreads = new double[60];

	public double[] m_daemonThreads = new double[60];

	public double[] m_totalThreads = new double[60];

	public double[] m_newThreads = new double[60];

	public double[] m_catThreads = new double[60];

	public double[] m_pigeonTheads = new double[60];

	public double[] m_catMessageProduced = new double[60];

	public double[] m_addCatMessageProduced = new double[60];

	public double[] m_catMessageOverflow = new double[60];

	public double[] m_addCatMessageOverflow = new double[60];

	public double[] m_catMessageSize = new double[60];

	public double[] m_addCatMessageSize = new double[60];

	public double[] m_gcCount = new double[60];

	public double[] m_addGcCount = new double[60];

	public double[] m_heapUsage = new double[60];

	public double[] m_noneHeapUsage = new double[60];

	public double[] m_diskFree = new double[60];

	public double[] m_diskUseable = new double[60];

	public double[] m_systemLoadAverage = new double[60];

	private GraphBuilder m_builder;

	public DisplayHeartbeat() {

	}

	public DisplayHeartbeat(GraphBuilder builder) {
		m_builder = builder;
	}

	public DisplayHeartbeat display(HeartbeatReport report, String ip) {
		if (report == null) {
			return this;
		}
		Machine machine = report.getMachines().get(ip);
		if (machine == null) {
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
			m_catThreads[minute] = period.getCatThreadCount();
			m_pigeonTheads[minute] = period.getPigeonThreadCount();
			m_catMessageProduced[minute] = period.getCatMessageProduced();
			m_catMessageOverflow[minute] = period.getCatMessageOverflow();
			period.setCatMessageSize(period.getCatMessageSize() / 8 / SIZE );
			m_catMessageSize[minute] = period.getCatMessageSize();
			m_gcCount[minute] = period.getGcCount();

			period.setHeapUsage(period.getHeapUsage() / SIZE / SIZE);
			m_heapUsage[minute] = period.getHeapUsage();

			period.setNoneHeapUsage(period.getNoneHeapUsage() / SIZE / SIZE);
			m_noneHeapUsage[minute] = period.getNoneHeapUsage();

			period.setDiskFree(period.getDiskFree() / SIZE / SIZE / SIZE);
			m_diskFree[minute] = period.getDiskFree();

			period.setDiskUseable(period.getDiskUseable() / SIZE / SIZE / SIZE);
			m_diskUseable[minute] = period.getDiskUseable();
			m_systemLoadAverage[minute] = period.getSystemLoadAverage();
		}
		for (int i = 1; i <= 59; i++) {
			double d = m_totalThreads[i] - m_totalThreads[i - 1];
			if (d < 0) {
				d = m_totalThreads[i];
			}
			m_newThreads[i] = d;
			
			double gc = m_gcCount[i] - m_gcCount[i-1];
			if(gc<0){
				d = m_gcCount[i];
			}
			m_addGcCount[i]=gc;
			
			double addMessageCount = m_catMessageProduced[i] - m_catMessageProduced[i-1];
			if(addMessageCount<0){
				addMessageCount = m_catMessageProduced[i];
			}
			m_addCatMessageProduced[i]=addMessageCount;
			
			double addMessageSize = m_catMessageSize[i] - m_catMessageSize[i-1];
			if(addMessageSize<0){
				addMessageSize = m_catMessageSize[i];
			}
			m_addCatMessageSize[i]=addMessageSize;
			
			double addMessageFlow = m_catMessageOverflow[i] - m_catMessageOverflow[i-1];
			if(addMessageFlow<0){
				addMessageFlow = m_catMessageOverflow[i];
			}
			m_addCatMessageOverflow[i]=addMessageFlow;
			
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

	public String getCatThreadGraph() {
		return m_builder.build(new HeartbeatPayload(4, "Cat Started Thread", "Minute", "Count", m_catThreads));
	}

	public String getPigeonTheadGraph() {
		return m_builder.build(new HeartbeatPayload(5, "Piegeon Started Thread", "Minute", "Count", m_pigeonTheads));
	}

	public String getCatMessageProducedGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Cat Message Produced / Minute", "Minute", "Count",
				m_addCatMessageProduced));
	}

	public String getCatMessageOverflowGraph() {
		return m_builder.build(new HeartbeatPayload(1, "Cat Message Overflow / Minute", "Minute", "Count",
		      m_addCatMessageOverflow));
	}

	public String getCatMessageSizeGraph() {
		return m_builder.build(new HeartbeatPayload(2, "Cat Message Size / Minute", "Minute", "KB", m_addCatMessageSize));
	}

	public String getGcCountGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Gc Count", "Minute", "Count", m_addGcCount));
	}

	public String getSystemLoadAverageGraph() {
		return m_builder.build(new HeartbeatPayload(1, "SystemLoad", "Minute", "", m_systemLoadAverage));
	}

	public String getHeapUsageGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Heap Usage", "Minute", "MB", m_heapUsage));
	}

	public String getNoneHeapUsageGraph() {
		return m_builder.build(new HeartbeatPayload(1, "None Heap Usage", "Minute", "MB", m_noneHeapUsage));
	}

	public String getDiskFreeGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Disk Free", "Minute", "GB", m_diskFree));
	}

	public String getDiskUseableGraph() {
		return m_builder.build(new HeartbeatPayload(1, "Disk Useable", "Minute", "GB", m_diskUseable));
	}

	public static class HeartbeatPayload extends AbstractGraphPayload {
		private String[] m_labels;

		private double[] m_values;

		private int m_index;

		private String m_idPrefix;

		public HeartbeatPayload(int index, String title, String axisXLabel, String axisYLabel, double[] values) {
			super(title, axisXLabel, axisYLabel);

			m_idPrefix = title;
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
			return m_index % 3 * getDisplayWidth();
		}

		@Override
		public int getOffsetY() {
			return m_index / 3 * (getDisplayHeight() + 20);
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
