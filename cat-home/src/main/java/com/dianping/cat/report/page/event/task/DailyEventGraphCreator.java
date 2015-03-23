package com.dianping.cat.report.page.event.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.DailyGraph;

public class DailyEventGraphCreator {

	private List<DailyGraph> m_graphs = new ArrayList<DailyGraph>();

	private EventReport m_report;

	public List<DailyGraph> buildDailygraph(EventReport report) {
		m_report = report;
		new EventReportVisitor().visitEventReport(report);

		return m_graphs;
	}

	private DailyGraph buildDailyGraph(String ip) {
		DailyGraph graph = new DailyGraph();

		graph.setDomain(m_report.getDomain());
		graph.setPeriod(m_report.getStartTime());
		graph.setName(EventAnalyzer.ID);
		graph.setIp(ip);
		graph.setType(3);
		graph.setCreationDate(new Date());
		return graph;
	}

	public class EventReportVisitor extends BaseVisitor {

		private String m_currentIp;

		private String m_currentType;

		private DailyGraph m_currentDailygraph;

		private StringBuilder m_summaryContent;

		private StringBuilder m_detailContent;

		@Override
		public void visitMachine(Machine machine) {
			m_currentIp = machine.getIp();
			m_currentDailygraph = buildDailyGraph(m_currentIp);
			m_graphs.add(m_currentDailygraph);
			m_summaryContent = new StringBuilder();
			m_detailContent = new StringBuilder();

			super.visitMachine(machine);
			m_currentDailygraph.setDetailContent(m_detailContent.toString());
			m_currentDailygraph.setSummaryContent(m_summaryContent.toString());
		}

		@Override
		public void visitName(EventName name) {
			// TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT
			m_detailContent.append(m_currentType).append('\t');
			m_detailContent.append(name.getId()).append('\t');
			m_detailContent.append(name.getTotalCount()).append('\t');
			m_detailContent.append(name.getFailCount()).append('\t').append('\n');
			super.visitName(name);
		}

		@Override
		public void visitType(EventType type) {
			// TYPE, TOTAL_COUNT, FAILURE_COUNT
			m_currentType = type.getId();
			m_summaryContent.append(type.getId()).append('\t');
			m_summaryContent.append(type.getTotalCount()).append('\t');
			m_summaryContent.append(type.getFailCount()).append('\t').append('\n');
			super.visitType(type);
		}
	}
}
