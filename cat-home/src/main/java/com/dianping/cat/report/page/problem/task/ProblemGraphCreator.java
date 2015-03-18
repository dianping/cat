/**
 * 
 */
package com.dianping.cat.report.page.problem.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.task.GraphLine;
import com.dianping.cat.report.task.TaskHelper;

public class ProblemGraphCreator {

	public List<Graph> splitReportToGraphs(Date reportPeriod, String reportDomain, String reportName,
	      ProblemReport report) {
		Set<String> ips = report.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1);

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
			Machine machine = report.findOrCreateMachine(ip);

			for (Entity entity : machine.getEntities().values()) {
				Map<String, JavaThread> threads = entity.getThreads();
				String type = entity.getType();
				String status = entity.getStatus();

				for (Entry<String, JavaThread> problemEntry : threads.entrySet()) {
					JavaThread thread = problemEntry.getValue();

					for (Entry<Integer, Segment> segmentEntry : thread.getSegments().entrySet()) {
						Segment segment = segmentEntry.getValue();
						int minute = segment.getId();
						int count = segment.getMessages().size();
						String summaryKey = type;

						GraphLine summaryLine = summaryCache.get(summaryKey);
						if (summaryLine == null) {
							summaryLine = new GraphLine();
							summaryLine.minuteCounts = new int[60];
							summaryCache.put(summaryKey, summaryLine);
						}
						summaryLine.totalCount = summaryLine.totalCount + count;
						summaryLine.minuteCounts[minute] = summaryLine.minuteCounts[minute] + count;

						GraphLine allSummaryLine = allSummaryCache.get(summaryKey);
						if (allSummaryLine == null) {
							allSummaryLine = new GraphLine();
							allSummaryLine.minuteCounts = new int[60];
							allSummaryCache.put(summaryKey, allSummaryLine);
						}
						allSummaryLine.totalCount = allSummaryLine.totalCount + count;
						allSummaryLine.minuteCounts[minute] = allSummaryLine.minuteCounts[minute] + count;

						String detailKey = type + "\t" + status;
						GraphLine detailLine = detailCache.get(detailKey);
						if (detailLine == null) {
							detailLine = new GraphLine();
							detailLine.minuteCounts = new int[60];
							detailCache.put(detailKey, detailLine);
						}
						detailLine.totalCount = detailLine.totalCount + count;
						detailLine.minuteCounts[minute] = detailLine.minuteCounts[minute] + count;

						GraphLine allDetailLine = allDetailCache.get(detailKey);
						if (allDetailLine == null) {
							allDetailLine = new GraphLine();
							allDetailLine.minuteCounts = new int[60];
							allDetailCache.put(detailKey, allDetailLine);
						}
						allDetailLine.totalCount = allDetailLine.totalCount + count;
						allDetailLine.minuteCounts[minute] = allDetailLine.minuteCounts[minute] + count;
					}
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