/**
 * 
 */
package com.dianping.cat.report.task.heartbeat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.dianping.cat.consumer.core.dal.Graph;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.GraphCreator;
import com.dianping.cat.report.task.spi.GraphLine;

public class HeartbeatGraphCreator implements GraphCreator<HeartbeatReport> {

	private void cacheHeartbeatColumn(Map<String, GraphLine> detailCache, int minute, Number value, String key) {
		GraphLine detailLine = detailCache.get(key);
		if (detailLine == null) {
			detailLine = new GraphLine();
			detailLine.minuteNumbers = new double[60];
			detailCache.put(key, detailLine);
		}
		detailLine.minuteNumbers[minute] = value.doubleValue();
	}

	@Override
	public List<Graph> splitReportToGraphs(Date reportPeriod, String domainName, String reportName,
	      HeartbeatReport heartbeatReport) {
		Set<String> ips = heartbeatReport.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size());

		for (String ip : ips) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(domainName);
			graph.setName(reportName);
			graph.setPeriod(reportPeriod);
			graph.setType(3);
			com.dianping.cat.consumer.heartbeat.model.entity.Machine machine = heartbeatReport.getMachines().get(ip);

			if (machine == null) {
				continue;
			}
			List<Period> periods = machine.getPeriods();

			Map<String, GraphLine> detailCache = new TreeMap<String, GraphLine>();

			for (Period period : periods) {
				int minute = period.getMinute();

				String key = "CatMessageSize";
				Number value = period.getCatMessageSize();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "CatMessageOverflow";
				value = period.getCatMessageOverflow();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "CatMessageProduced";
				value = period.getCatMessageProduced();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				List<Disk> disks = period.getDisks();
				for (Disk d : disks) {
					key = "Disk " + d.getPath();
					value = d.getFree();
					cacheHeartbeatColumn(detailCache, minute, value, key);
				}

				key = "MemoryFree";
				value = period.getMemoryFree();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "HeapUsage";
				value = period.getHeapUsage();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "NoneHeapUsage";
				value = period.getNoneHeapUsage();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "SystemLoadAverage";
				value = period.getSystemLoadAverage();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "OldGcCount";
				value = period.getOldGcCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "NewGcCount";
				value = period.getNewGcCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "PigeonStartedThread";
				value = period.getPigeonThreadCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "CatThreadCount";
				value = period.getCatThreadCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "TotalStartedThread";
				value = period.getTotalStartedCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "DaemonThread";
				value = period.getDaemonCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "ActiveThread";
				value = period.getThreadCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "HttpThread";
				value = period.getHttpThreadCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);
			}

			for (Entry<String, GraphLine> entry : detailCache.entrySet()) {
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

			StringBuilder sb = new StringBuilder(64 * detailCache.size());
			for (Entry<String, GraphLine> entry : detailCache.entrySet()) {
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

			graph.setDetailContent(sb.toString());
			graph.setCreationDate(new Date());
			graphs.add(graph);
		}
		return graphs;
	}
}
