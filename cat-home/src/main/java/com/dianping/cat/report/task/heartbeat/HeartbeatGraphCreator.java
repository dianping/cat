/**
 * 
 */
package com.dianping.cat.report.task.heartbeat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.GraphLine;

public class HeartbeatGraphCreator {

	private List<Graph> m_graphs = new ArrayList<Graph>();

	private HeartbeatReport m_report;

	public List<Graph> buildGraph(HeartbeatReport report) {
		m_report = report;
		new HeartbeatReportVisitor().visitHeartbeatReport(report);
		
		return m_graphs;
	}

	private class HeartbeatReportVisitor extends BaseVisitor {

		private int m_currentMinute;

		private Map<String, GraphLine> m_detailCache = new TreeMap<String, GraphLine>();

		private void cacheHeartbeatColumn(String key, Number value) {
			GraphLine detailLine = m_detailCache.get(key);
			if (detailLine == null) {
				detailLine = new GraphLine();
				detailLine.minuteNumbers = new double[60];
				m_detailCache.put(key, detailLine);
			}
			detailLine.minuteNumbers[m_currentMinute] = value.doubleValue();
		}

		private Graph buildGraph(String ip) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(m_report.getDomain());
			graph.setName(HeartbeatAnalyzer.ID);
			graph.setPeriod(m_report.getStartTime());
			graph.setType(3);
			graph.setCreationDate(new Date());
			return graph;
		}

		@Override
		public void visitMachine(Machine machine) {
			super.visitMachine(machine);
			for (Entry<String, GraphLine> entry : m_detailCache.entrySet()) {
				GraphLine line = entry.getValue();
				double[] numbers = line.minuteNumbers;
				double minValue = numbers[0];
				double maxValue = minValue;
				double sum = minValue;
				double sum2 = sum * sum;

				for (int i = 1; i < numbers.length; i++) {
					double n = numbers[i];
					if (n > maxValue) {
						maxValue = n;
					}
					if (n < minValue) {
						minValue = n;
					}
					sum += n;
					sum2 += n * n;
				}

				line.min = minValue;
				line.max = maxValue;
				line.sum = sum;
				line.sum2 = sum2;
			}

			StringBuilder sb = new StringBuilder(64 * m_detailCache.size());
			for (Entry<String, GraphLine> entry : m_detailCache.entrySet()) {
				GraphLine value = entry.getValue();
				sb.append(entry.getKey());
				sb.append('\t');
				sb.append(value.min);
				sb.append('\t');
				sb.append(value.max);
				sb.append('\t');
				sb.append(value.sum);
				sb.append('\t');
				sb.append(value.sum2);
				sb.append('\t');
				sb.append(TaskHelper.join(value.minuteNumbers, ','));
				sb.append('\n');
			}
			Graph graph = buildGraph(machine.getIp());
			graph.setDetailContent(sb.toString());
			m_graphs.add(graph);
		}

		@Override
		public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
			super.visitHeartbeatReport(heartbeatReport);
		}

		@Override
		public void visitPeriod(Period period) {
			m_currentMinute = period.getMinute();

			String key = "CatMessageSize";
			Number value = period.getCatMessageSize();
			cacheHeartbeatColumn(key, value);

			key = "CatMessageOverflow";
			value = period.getCatMessageOverflow();
			cacheHeartbeatColumn(key, value);

			key = "CatMessageProduced";
			value = period.getCatMessageProduced();
			cacheHeartbeatColumn(key, value);

			key = "MemoryFree";
			value = period.getMemoryFree();
			cacheHeartbeatColumn(key, value);

			key = "HeapUsage";
			value = period.getHeapUsage();
			cacheHeartbeatColumn(key, value);

			key = "NoneHeapUsage";
			value = period.getNoneHeapUsage();
			cacheHeartbeatColumn(key, value);

			key = "SystemLoadAverage";
			value = period.getSystemLoadAverage();
			cacheHeartbeatColumn(key, value);

			key = "OldGcCount";
			value = period.getOldGcCount();
			cacheHeartbeatColumn(key, value);

			key = "NewGcCount";
			value = period.getNewGcCount();
			cacheHeartbeatColumn(key, value);

			key = "PigeonStartedThread";
			value = period.getPigeonThreadCount();
			cacheHeartbeatColumn(key, value);

			key = "CatThreadCount";
			value = period.getCatThreadCount();
			cacheHeartbeatColumn(key, value);

			key = "TotalStartedThread";
			value = period.getTotalStartedCount();
			cacheHeartbeatColumn(key, value);

			key = "DaemonThread";
			value = period.getDaemonCount();
			cacheHeartbeatColumn(key, value);

			key = "ActiveThread";
			value = period.getThreadCount();
			cacheHeartbeatColumn(key, value);

			key = "HttpThread";
			value = period.getHttpThreadCount();
			cacheHeartbeatColumn(key, value);

			super.visitPeriod(period);

		}

		@Override
		public void visitDisk(Disk disk) {
			String key = "Disk " + disk.getPath();
			Number value = disk.getFree();

			cacheHeartbeatColumn(key, value);
			super.visitDisk(disk);
		}

	}
}
