package com.dianping.cat.report.task.utilization;

import java.util.HashSet;
import java.util.Set;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public class TransactionReportVisitor extends BaseVisitor {

	private String m_domain;

	private UtilizationReport m_report;

	private Set<String> m_types = new HashSet<String>();

	private static final String MEMCACHED = "Cache.memcached";

	public TransactionReportVisitor() {
		m_types.add("URL");
		m_types.add("Service");
		m_types.add("PigeonService");
		m_types.add("Call");
		m_types.add("PigeonCall");
		m_types.add("SQL");
		m_types.add(MEMCACHED);
	}

	public TransactionReportVisitor setUtilizationReport(UtilizationReport report) {
		m_report = report;
		return this;
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();
		if (Constants.ALL.equals(ip)) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_domain = transactionReport.getDomain();
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		String typeName = type.getId();
		Domain domain = m_report.findOrCreateDomain(m_domain);

		if ("Service".equals(typeName)) {
			typeName = "PigeonService";
		} else if ("Call".equals(typeName)) {
			typeName = "PigeonCall";
		} else if (typeName.startsWith(MEMCACHED)) {
			typeName = MEMCACHED;
		}
		if (m_types.contains(typeName)) {
			copyAttribute(type, domain.findOrCreateApplicationState(typeName));
		}
	}

	private void copyAttribute(TransactionType type, ApplicationState state) {
		state.setAvg95((state.getCount() * state.getAvg95() + type.getTotalCount() * type.getLine95Value())
		      / (state.getCount() + type.getTotalCount()));
		state.setSum(state.getSum() + type.getSum());
		state.setFailureCount(state.getFailureCount() + type.getFailCount());
		state.setCount(state.getCount() + type.getTotalCount());
		state.setFailurePercent(state.getFailureCount() * 1.0 / state.getCount());
		state.setAvg(state.getSum() * 1.0 / state.getCount());
	}

}
