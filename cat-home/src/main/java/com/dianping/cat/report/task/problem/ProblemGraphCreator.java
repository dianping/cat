/**
 * 
 */
package com.dianping.cat.report.task.problem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.GraphLine;

public class ProblemGraphCreator {

	private List<Graph> m_graphs = new ArrayList<Graph>();

	private ProblemReport m_report;

	public List<Graph> buildGraph(ProblemReport report) {
		m_report = report;
		report.accept(new ProblemReportVisitor());

		return m_graphs;
	}

	public class ProblemReportVisitor extends BaseVisitor {
		private Map<String, GraphLine> m_allDetailCache = new TreeMap<String, GraphLine>();

		private Map<String, GraphLine> m_allSummaryCache = new TreeMap<String, GraphLine>();

		private Map<String, GraphLine> m_detailCache;

		private Map<String, GraphLine> m_summaryCache;

		private String m_status;

		private String m_type;

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
		public void visitEntry(com.dianping.cat.consumer.problem.model.entity.Entry entry) {
			m_type = entry.getType();
			m_status = entry.getStatus();
			super.visitEntry(entry);
		}

		@Override
		public void visitMachine(Machine machine) {
			m_detailCache = new TreeMap<String, GraphLine>();
			m_summaryCache = new TreeMap<String, GraphLine>();
			super.visitMachine(machine);

			Graph graph = buildGraph(machine.getIp());
			StringBuilder summaryBuilder = new StringBuilder();
			
			for (Entry<String, GraphLine> summaryEntry : m_summaryCache.entrySet()) {
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
			
			for (Entry<String, GraphLine> detailEntry : m_detailCache.entrySet()) {
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
			m_graphs.add(graph);
		}

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			super.visitProblemReport(problemReport);
			Graph allGraph = buildGraph("all");
			StringBuilder summaryBuilder = new StringBuilder();
			
			for (Entry<String, GraphLine> summaryEntry : m_allSummaryCache.entrySet()) {
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
			
			for (Entry<String, GraphLine> detailEntry : m_allDetailCache.entrySet()) {
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
			m_graphs.add(allGraph);
		}

		@Override
		public void visitSegment(Segment segment) {
			int minute = segment.getId();
			int count = segment.getMessages().size();
			String summaryKey = m_type;
			GraphLine summaryLine = m_summaryCache.get(summaryKey);
			
			if (summaryLine == null) {
				summaryLine = new GraphLine();
				summaryLine.minuteCounts = new int[60];
				m_summaryCache.put(summaryKey, summaryLine);
			}
			summaryLine.totalCount = summaryLine.totalCount + count;
			summaryLine.minuteCounts[minute] = summaryLine.minuteCounts[minute] + count;

			GraphLine allSummaryLine = m_allSummaryCache.get(summaryKey);
			
			if (allSummaryLine == null) {
				allSummaryLine = new GraphLine();
				allSummaryLine.minuteCounts = new int[60];
				m_allSummaryCache.put(summaryKey, allSummaryLine);
			}
			allSummaryLine.totalCount = allSummaryLine.totalCount + count;
			allSummaryLine.minuteCounts[minute] = allSummaryLine.minuteCounts[minute] + count;

			String detailKey = m_type + "\t" + m_status;
			GraphLine detailLine = m_detailCache.get(detailKey);
			
			if (detailLine == null) {
				detailLine = new GraphLine();
				detailLine.minuteCounts = new int[60];
				m_detailCache.put(detailKey, detailLine);
			}
			detailLine.totalCount = detailLine.totalCount + count;
			detailLine.minuteCounts[minute] = detailLine.minuteCounts[minute] + count;

			GraphLine allDetailLine = m_allDetailCache.get(detailKey);
			
			if (allDetailLine == null) {
				allDetailLine = new GraphLine();
				allDetailLine.minuteCounts = new int[60];
				m_allDetailCache.put(detailKey, allDetailLine);
			}
			allDetailLine.totalCount = allDetailLine.totalCount + count;
			allDetailLine.minuteCounts[minute] = allDetailLine.minuteCounts[minute] + count;
			super.visitSegment(segment);
		}

		@Override
		public void visitThread(JavaThread thread) {
			super.visitThread(thread);
		}
	}

}
