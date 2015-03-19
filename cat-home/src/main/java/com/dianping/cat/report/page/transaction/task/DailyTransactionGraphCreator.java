package com.dianping.cat.report.page.transaction.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.DailyGraph;

public class DailyTransactionGraphCreator {

	private List<DailyGraph> m_graphs = new ArrayList<DailyGraph>();

	private TransactionReport m_report;

	public List<DailyGraph> buildDailygraph(TransactionReport report) {
		m_report = report;
		new TransactionReportVisitor().visitTransactionReport(report);

		return m_graphs;
	}

	private DailyGraph buildDailyGraph(String ip) {
		DailyGraph graph = new DailyGraph();

		graph.setDomain(m_report.getDomain());
		graph.setPeriod(m_report.getStartTime());
		graph.setName(TransactionAnalyzer.ID);
		graph.setIp(ip);
		graph.setType(3);
		graph.setCreationDate(new Date());
		return graph;
	}

	public class TransactionReportVisitor extends BaseVisitor {

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
		public void visitName(TransactionName name) {
			// TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
			m_detailContent.append(m_currentType).append('\t');
			m_detailContent.append(name.getId()).append('\t');
			m_detailContent.append(name.getTotalCount()).append('\t');
			m_detailContent.append(name.getFailCount()).append('\t');
			m_detailContent.append(name.getMin()).append('\t');
			m_detailContent.append(name.getMax()).append('\t');
			m_detailContent.append(name.getSum()).append('\t');
			m_detailContent.append(name.getSum2()).append('\t').append('\n');
			super.visitName(name);
		}

		@Override
		public void visitType(TransactionType type) {
			// TYPE, TOTAL_COUNT, FAILURE_COUNT, MIN, MAX, SUM, SUM2
			m_currentType = type.getId();
			m_summaryContent.append(type.getId()).append('\t');
			m_summaryContent.append(type.getTotalCount()).append('\t');
			m_summaryContent.append(type.getFailCount()).append('\t');
			m_summaryContent.append(type.getMin()).append('\t');
			m_summaryContent.append(type.getMax()).append('\t');
			m_summaryContent.append(type.getSum()).append('\t');
			m_summaryContent.append(type.getSum2()).append('\t').append('\n');
			super.visitType(type);
		}
	}
}
