package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.report.graph.AbstractGraphPayload;
import com.dianping.cat.report.graph.GraphBuilder;

public class DisplayHeartbeat {
	private static final int K = 1024;

	private double[] m_activeThreads = new double[60];

	private double[] m_addCatMessageOverflow = new double[60];

	private double[] m_addCatMessageProduced = new double[60];

	private double[] m_addCatMessageSize = new double[60];

	private double[] m_addNewGcCount = new double[60];

	private double[] m_addOldGcCount = new double[60];

	private GraphBuilder m_builder;

	private double[] m_catMessageOverflow = new double[60];

	private double[] m_catMessageProduced = new double[60];

	private double[] m_catMessageSize = new double[60];

	private double[] m_catThreads = new double[60];

	private double[] m_daemonThreads = new double[60];

	private double[] m_heapUsage = new double[60];

	private double[] m_httpThreads = new double[60];

	private double[] m_memoryFree = new double[60];

	private double[] m_newGcCount = new double[60];

	private double[] m_newThreads = new double[60];

	private double[] m_noneHeapUsage = new double[60];

	private double[] m_oldGcCount = new double[60];

	private List<Period> m_periods = new ArrayList<Period>();

	private double[] m_pigeonThreads = new double[60];

	private double[] m_systemLoadAverage = new double[60];

	private double[] m_totalThreads = new double[60];

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
			int minute = period.getMinute();

			m_periods.add(period);
			m_activeThreads[minute] = period.getThreadCount();
			m_daemonThreads[minute] = period.getDaemonCount();
			m_totalThreads[minute] = period.getTotalStartedCount();
			m_catThreads[minute] = period.getCatThreadCount();
			m_pigeonThreads[minute] = period.getPigeonThreadCount();
			m_httpThreads[minute] = period.getHttpThreadCount();
			m_catMessageProduced[minute] = period.getCatMessageProduced();
			m_catMessageOverflow[minute] = period.getCatMessageOverflow();
			period.setCatMessageSize((double) (period.getCatMessageSize() / K / K));
			m_catMessageSize[minute] = period.getCatMessageSize();
			m_newGcCount[minute] = period.getNewGcCount();
			m_oldGcCount[minute] = period.getOldGcCount();

			period.setHeapUsage(period.getHeapUsage() / K / K);
			m_heapUsage[minute] = period.getHeapUsage();

			period.setNoneHeapUsage(period.getNoneHeapUsage() / K / K);
			m_noneHeapUsage[minute] = period.getNoneHeapUsage();

			period.setMemoryFree(period.getMemoryFree() / K / K);
			m_memoryFree[minute] = period.getMemoryFree();

			m_systemLoadAverage[minute] = period.getSystemLoadAverage();
		}

		m_newThreads = getAddedCount(m_totalThreads);
		m_addNewGcCount = getAddedCount(m_newGcCount);
		m_addOldGcCount = getAddedCount(m_oldGcCount);
		m_addCatMessageProduced = getAddedCount(m_catMessageProduced);
		m_addCatMessageSize = getAddedCount(m_catMessageSize);
		m_addCatMessageOverflow = getAddedCount(m_catMessageOverflow);

		return this;
	}

	public String getActiceThreadGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Active Thread", "Minute", "Count", m_activeThreads));
	}

	public double[] getActiveThreads() {
		return m_activeThreads;
	}

	public double[] getAddCatMessageOverflow() {
		return m_addCatMessageOverflow;
	}

	public double[] getAddCatMessageProduced() {
		return m_addCatMessageProduced;
	}

	public double[] getAddCatMessageSize() {
		return m_addCatMessageSize;
	}

	private double[] getAddedCount(double[] source) {
		double[] result = new double[60];
		for (int i = 1; i <= 59; i++) {
			if (source[i - 1] > 0) {
				double d = source[i] - source[i - 1];
				if (d < 0) {
					d = source[i];
				}
				result[i] = d;
			}
		}
		return result;
	}

	public double[] getAddNewGcCount() {
		return m_addNewGcCount;
	}

	public double[] getAddOldGcCount() {
		return m_addOldGcCount;
	}

	public GraphBuilder getBuilder() {
		return m_builder;
	}

	public double[] getCatMessageOverflow() {
		return m_catMessageOverflow;
	}

	public String getCatMessageOverflowGraph() {
		return m_builder.build(new HeartbeatPayload(1, "Cat Message Overflow / Minute", "Minute", "Count",
		      m_addCatMessageOverflow));
	}

	public double[] getCatMessageProduced() {
		return m_catMessageProduced;
	}

	public String getCatMessageProducedGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Cat Message Produced / Minute", "Minute", "Count",
		      m_addCatMessageProduced));
	}

	public double[] getCatMessageSize() {
		return m_catMessageSize;
	}

	public String getCatMessageSizeGraph() {
		return m_builder.build(new HeartbeatPayload(2, "Cat Message Size / Minute", "Minute", "MB", m_addCatMessageSize));
	}

	public String getCatThreadGraph() {
		return m_builder.build(new HeartbeatPayload(1, "Cat Thread", "Minute", "Count", m_catThreads));
	}

	public double[] getCatThreads() {
		return m_catThreads;
	}

	public double[] getDaemonThreads() {
		return m_daemonThreads;
	}

	public String getDeamonThreadGraph() {
		return m_builder.build(new HeartbeatPayload(6, "Daemon Thread", "Minute", "Count", m_daemonThreads));
	}

	public List<String> getDiskNames() {
		List<String> result = new ArrayList<String>();

		if (!m_periods.isEmpty()) {
			List<Disk> disks = m_periods.get(0).getDisks();
			int len = disks.size();

			for (int i = 0; i < len; i++) {
				String path = disks.get(i).getPath();
				result.add(path);
			}
		}
		return result;
	}

	public int getDisks() {
		if (!m_periods.isEmpty()) {
			List<Disk> disks = m_periods.get(0).getDisks();

			return disks.size();
		} else {
			return 0;
		}
	}

	public String getDisksGraph() {
		StringBuilder sb = new StringBuilder(4096);

		if (!m_periods.isEmpty()) {
			List<Disk> disks = m_periods.get(0).getDisks();
			int len = disks.size();

			for (int i = 0; i < len; i++) {
				double[] values = new double[60];

				for (Period period : m_periods) {
					int minute = period.getMinute();
					List<Disk> ds = period.getDisks();

					if (ds.size() > i) {
						Disk disk = ds.get(i);

						values[minute] = disk.getFree() / K / K / K;
					}
				}

				String path = disks.get(i).getPath();
				String graph = m_builder.build(new HeartbeatPayload(i, "Disk Free (" + path + ")", "Minute", "GB", values));

				sb.append(graph);
			}
		}

		return sb.toString();
	}

	public List<double[]> getDiskValues() {
		List<double[]> result = new ArrayList<double[]>();
		if (!m_periods.isEmpty()) {
			List<Disk> disks = m_periods.get(0).getDisks();
			int len = disks.size();

			for (int i = 0; i < len; i++) {
				double[] values = new double[60];

				for (Period period : m_periods) {
					int minute = period.getMinute();
					Disk disk = period.getDisks().get(i);

					values[minute] = disk.getFree() / K / K / K;
				}
				result.add(values);
			}
		}
		return result;
	}

	public double[] getHeapUsage() {
		return m_heapUsage;
	}

	public String getHeapUsageGraph() {
		return m_builder.build(new HeartbeatPayload(1, "Heap Usage", "Minute", "MB", m_heapUsage));
	}

	public String getHttpTheadGraph() {
		return m_builder.build(new HeartbeatPayload(0, "HTTP Thread", "Minute", "Count", m_httpThreads));
	}

	public double[] getHttpThreads() {
		return m_httpThreads;
	}

	public double[] getMemoryFree() {
		return m_memoryFree;
	}

	public String getMemoryFreeGraph() {
		return m_builder.build(new HeartbeatPayload(0, "Memory Free", "Minute", "MB", m_memoryFree));
	}

	public double[] getNewGcCount() {
		return m_newGcCount;
	}

	public String getNewGcCountGraph() {
		return m_builder.build(new HeartbeatPayload(0, "NewGc Count", "Minute", "Count", m_addNewGcCount));
	}

	public double[] getNewThreads() {
		return m_newThreads;
	}

	public double[] getNoneHeapUsage() {
		return m_noneHeapUsage;
	}

	public String getNoneHeapUsageGraph() {
		return m_builder.build(new HeartbeatPayload(2, "None Heap Usage", "Minute", "MB", m_noneHeapUsage));
	}

	public double[] getOldGcCount() {
		return m_oldGcCount;
	}

	public String getOldGcCountGraph() {
		return m_builder.build(new HeartbeatPayload(1, "OldGc Count", "Minute", "Count", m_addOldGcCount));
	}

	public List<Period> getPeriods() {
		return m_periods;
	}

	public String getPigeonTheadGraph() {
		return m_builder.build(new HeartbeatPayload(2, "Pigeon Thread", "Minute", "Count", m_pigeonThreads));
	}

	public double[] getPigeonThreads() {
		return m_pigeonThreads;
	}

	public String getStartedThreadGraph() {
		return m_builder.build(new HeartbeatPayload(1, "Started Thread", "Minute", "Count", m_newThreads));
	}

	public double[] getSystemLoadAverage() {
		return m_systemLoadAverage;
	}

	public String getSystemLoadAverageGraph() {
		return m_builder.build(new HeartbeatPayload(2, "System Load Average", "Minute", "", m_systemLoadAverage));
	}

	public String getTotalThreadGraph() {
		return m_builder.build(new HeartbeatPayload(2, "Total Started Thread", "Minute", "Count", m_totalThreads));
	}

	public double[] getTotalThreads() {
		return m_totalThreads;
	}

	public static class HeartbeatPayload extends AbstractGraphPayload {
		private String m_idPrefix;

		private int m_index;

		private String[] m_labels;

		private double[] m_values;

		public HeartbeatPayload(int index, String title, String axisXLabel, String axisYLabel, double[] values) {
			super(title, axisXLabel, axisYLabel);

			m_idPrefix = title;
			m_index = index;
			m_labels = new String[61];

			for (int i = 0; i <= 60; i++) {
				m_labels[i] = String.valueOf(i);
			}

			if (values == null) {
				m_values = new double[0];
			} else {
				m_values = Arrays.copyOf(values, values.length);
			}
		}

		@Override
		public String getAxisXLabel(int index) {
			if (index % 5 == 0 && index < m_labels.length) {
				return m_labels[index];
			} else {
				return "";
			}
		}

		@Override
		public int getDisplayHeight() {
			return (int) (super.getDisplayHeight() * 0.7);
		}

		@Override
		public int getDisplayWidth() {
			return (int) (super.getDisplayWidth() * 0.66);
		}

		@Override
		public String getIdPrefix() {
			return m_idPrefix;
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
		public int getWidth() {
			return super.getWidth() + 120;
		}

		@Override
		public boolean isStandalone() {
			return false;
		}

		@Override
		protected double[] loadValues() {
			return m_values;
		}
	}
}
