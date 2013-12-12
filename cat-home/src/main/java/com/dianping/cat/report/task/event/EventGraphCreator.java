/**
 * 
 */
package com.dianping.cat.report.task.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.task.spi.GraphLine;

public class EventGraphCreator {
	
	private List<Graph> m_graphs = new ArrayList<Graph>();

	private EventReport m_report;

	public List<Graph> buildGraph(EventReport report) {
		m_report = report;
		report.accept(new EventReportVisitor());

		return m_graphs;
	}
	
	private class EventReportVisitor extends  BaseVisitor{

		String m_currentIp;

		String m_currentType;

		int m_currentIndex;

		GraphLine m_currentDetail;

		GraphLine m_currentSummary;

		GraphLine m_allDetail;

		GraphLine m_allSummary;

		Map<String, GraphLine> m_allDetails = new HashMap<String, GraphLine>();

		Map<String, GraphLine> m_allSummaries = new HashMap<String, GraphLine>();

		StringBuilder m_summaryContent;

		StringBuilder m_detailContent;

		StringBuilder m_allSummaryContent;

		StringBuilder m_allDetailContent;

		private void addRange(GraphLine graphLine, int index, Range range) {
			graphLine.totalCounts[index] += range.getCount();
			graphLine.failCounts[index] += range.getFails();
		}

		private String arrayToString(long[] array) {
			StringBuilder sb = new StringBuilder();
			int size = 12;
			for (int i = 0; i < size; i++) {
				sb.append(array[i]);
				if (i < 11) {
					sb.append(',');
				}
			}
			return sb.toString();
		}

		private void buildContent(StringBuilder content, String key, GraphLine graphLine) {
			content.append(key).append('\t');
			content.append(arrayToString(graphLine.totalCounts)).append('\t');
			content.append(arrayToString(graphLine.failCounts)).append('\n');
		}

		private void buildContent(StringBuilder content, String type, EventName name, GraphLine graphLine) {
			content.append(type).append('\t');
			content.append(name.getId()).append('\t');
			content.append(arrayToString(graphLine.totalCounts)).append('\t');
			content.append(arrayToString(graphLine.failCounts)).append('\n');
		}

		private void buildContent(StringBuilder content, EventType type, GraphLine graphLine) {
			content.append(type.getId()).append('\t');
			content.append(arrayToString(graphLine.totalCounts)).append('\t');
			content.append(arrayToString(graphLine.failCounts)).append('\n');
		}
		
		private Graph buildGraph(String ip) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(m_report.getDomain());
			graph.setName(EventAnalyzer.ID);
			graph.setPeriod(m_report.getStartTime());
			graph.setType(3);
			graph.setCreationDate(new Date());
			return graph;
		}

		private void buildGraphLine(GraphLine graphLine, EventName name, boolean isNew) {
			if (isNew) {
				graphLine.totalCounts = new long[12];
				graphLine.failCounts = new long[12];
				graphLine.sums = new double[12];
			}
		}

		private void buildGraphLine(GraphLine graphLine, EventType type, boolean isNew) {
			if (isNew) {
				graphLine.totalCounts = new long[12];
				graphLine.failCounts = new long[12];
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			m_currentIp = machine.getIp();
			Graph graph = buildGraph(m_currentIp);
			m_graphs.add(graph);
			m_summaryContent = new StringBuilder();
			m_detailContent = new StringBuilder();

			super.visitMachine(machine);
			graph.setDetailContent(m_detailContent.toString());
			graph.setSummaryContent(m_summaryContent.toString());
		}

		@Override
		public void visitName(EventName name) {
			String key = m_currentType + "\t" + name.getId();
			m_allDetail = m_allDetails.get(key);
			if (m_allDetail == null) {
				m_allDetail = new GraphLine();
				m_allDetails.put(key, m_allDetail);
				buildGraphLine(m_allDetail, name, true);
			} else {
				buildGraphLine(m_allDetail, name, false);
			}
			m_currentDetail = new GraphLine();
			buildGraphLine(m_currentDetail, name, true);
			m_currentIndex = 0;

			super.visitName(name);
			buildContent(m_detailContent, m_currentType, name, m_currentDetail);
		}

		@Override
		public void visitRange(Range range) {
			addRange(m_currentDetail, m_currentIndex, range);
			addRange(m_currentSummary, m_currentIndex, range);
			addRange(m_allDetail, m_currentIndex, range);
			addRange(m_allSummary, m_currentIndex, range);

			m_currentIndex++;
		}

		@Override
		public void visitEventReport(EventReport eventReport) {
			Graph allGraph = buildGraph("all");
			m_graphs.add(allGraph);
			m_allDetailContent = new StringBuilder();
			m_allSummaryContent = new StringBuilder();
			super.visitEventReport(eventReport);
			for (Entry<String, GraphLine> entry : m_allDetails.entrySet()) {
				buildContent(m_allDetailContent, entry.getKey(), entry.getValue());
			}
			for (Entry<String, GraphLine> entry : m_allSummaries.entrySet()) {
				buildContent(m_allSummaryContent, entry.getKey(), entry.getValue());
			}
			allGraph.setDetailContent(m_allDetailContent.toString());
			allGraph.setSummaryContent(m_allSummaryContent.toString());
		}

		@Override
		public void visitType(EventType type) {
			// TYPE, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
			m_currentType = type.getId();
			String key = m_currentType;
			m_allSummary = m_allSummaries.get(key);
			if (m_allSummary == null) {
				m_allSummary = new GraphLine();
				m_allSummaries.put(key, m_allSummary);
				buildGraphLine(m_allSummary, type, true);
			} else {
				buildGraphLine(m_allSummary, type, false);
			}
			m_currentSummary = new GraphLine();
			buildGraphLine(m_currentSummary, type, true);

			super.visitType(type);

			buildContent(m_summaryContent, type, m_currentSummary);
		}
	}



}
