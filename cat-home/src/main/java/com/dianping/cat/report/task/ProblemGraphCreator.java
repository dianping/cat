/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.hadoop.dal.Graph;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public class ProblemGraphCreator implements GraphCreator<ProblemReport> {

	@Override
	public List<Graph> splitReportToGraphs(Date reportPeriod, String reportDomain, String reportName, ProblemReport report) {
		Set<String> ips = report.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1); // all and every machine
		Map<String, GraphLine> allDetailCache = new TreeMap<String, GraphLine>();
		Map<String, GraphLine> allSummaryCache = new TreeMap<String, GraphLine>();

		for (String ip : ips) {
			Map<String, GraphLine> detailCache = new TreeMap<String, GraphLine>();
			Map<String, GraphLine> summaryCache = new TreeMap<String, GraphLine>();
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(reportDomain);
			graph.setName(reportName);
			graph.setPeriod(reportPeriod);
			graph.setType(3);
			com.dianping.cat.consumer.problem.model.entity.Machine machine = report.getMachines().get(ip);
			//Map<String, JavaThread> types = machine.getThreads();
			Map<String, JavaThread> types = null;

			for (Entry<String, JavaThread> transactionEntry : types.entrySet()) {
				JavaThread thread = transactionEntry.getValue();
				for (Entry<Integer, Segment> segmentEntry : thread.getSegments().entrySet()) {
					Segment segment = segmentEntry.getValue();
					int minute = segment.getId();
//					for (com.dianping.cat.consumer.problem.model.entity.Entry entry : segment.getEntries()) {
//						String summaryKey = entry.getType();
//						GraphLine summaryLine = summaryCache.get(summaryKey);
//						if (summaryLine == null) {
//							summaryLine = new GraphLine();
//							summaryLine.minuteCounts = new int[60];
//							summaryCache.put(summaryKey, summaryLine);
//						}
//						summaryLine.totalCount++;
//						summaryLine.minuteCounts[minute]++;
//
//						GraphLine allSummaryLine = allSummaryCache.get(summaryKey);
//						if (allSummaryLine == null) {
//							allSummaryLine = new GraphLine();
//							allSummaryLine.minuteCounts = new int[60];
//							allSummaryCache.put(summaryKey, allSummaryLine);
//						}
//						allSummaryLine.totalCount++;
//						allSummaryLine.minuteCounts[minute]++;
//
//						String detailKey = entry.getType() + "\t" + entry.getStatus();
//						GraphLine detailLine = detailCache.get(detailKey);
//						if (detailLine == null) {
//							detailLine = new GraphLine();
//							detailLine.minuteCounts = new int[60];
//							detailCache.put(detailKey, detailLine);
//						}
//						detailLine.totalCount++;
//						detailLine.minuteCounts[minute]++;
//
//						GraphLine allDetailLine = allDetailCache.get(detailKey);
//						if (allDetailLine == null) {
//							allDetailLine = new GraphLine();
//							allDetailLine.minuteCounts = new int[60];
//							allDetailCache.put(detailKey, allDetailLine);
//						}
//						allDetailLine.totalCount++;
//						allDetailLine.minuteCounts[minute]++;
//					}
				}

			}

			StringBuilder summaryBuilder = new StringBuilder();
			for (Entry<String, GraphLine> summaryEntry : summaryCache.entrySet()) {
				GraphLine summaryLine = summaryEntry.getValue();
				summaryBuilder.append(summaryEntry.getKey());
				summaryBuilder.append("\t");
				summaryBuilder.append(summaryLine.totalCount);
				summaryBuilder.append("\t");
				summaryBuilder.append(TaskHelper.join(summaryLine.minuteCounts, ','));
				summaryBuilder.append("\n");
			}
			graph.setSummaryContent(summaryBuilder.toString());

			StringBuilder detailBuilder = new StringBuilder();
			for (Entry<String, GraphLine> detailEntry : detailCache.entrySet()) {
				GraphLine detailLine = detailEntry.getValue();
				detailBuilder.append(detailEntry.getKey());
				detailBuilder.append("\t");
				detailBuilder.append(detailLine.totalCount);
				detailBuilder.append("\t");
				detailBuilder.append(TaskHelper.join(detailLine.minuteCounts, ','));
				detailBuilder.append("\n");
			}
			graph.setDetailContent(detailBuilder.toString());

			graph.setCreationDate(new Date());
			graphs.add(graph);
		}

		Graph allGraph = new Graph();
		allGraph.setIp("all");
		allGraph.setDomain(reportDomain);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeriod);
		allGraph.setType(3);

		StringBuilder summaryBuilder = new StringBuilder();
		for (Entry<String, GraphLine> summaryEntry : allSummaryCache.entrySet()) {
			GraphLine summaryLine = summaryEntry.getValue();
			summaryBuilder.append(summaryEntry.getKey());
			summaryBuilder.append("\t");
			summaryBuilder.append(summaryLine.totalCount);
			summaryBuilder.append("\t");
			summaryBuilder.append(TaskHelper.join(summaryLine.minuteCounts, ','));
			summaryBuilder.append("\n");
		}
		allGraph.setSummaryContent(summaryBuilder.toString());

		StringBuilder detailBuilder = new StringBuilder();
		for (Entry<String, GraphLine> detailEntry : allDetailCache.entrySet()) {
			GraphLine detailLine = detailEntry.getValue();
			detailBuilder.append(detailEntry.getKey());
			detailBuilder.append("\t");
			detailBuilder.append(detailLine.totalCount);
			detailBuilder.append("\t");
			detailBuilder.append(TaskHelper.join(detailLine.minuteCounts, ','));
			detailBuilder.append("\n");
		}
		allGraph.setDetailContent(detailBuilder.toString());

		allGraph.setCreationDate(new Date());

		graphs.add(allGraph);

		return graphs;
	}

}
