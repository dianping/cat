package com.dianping.cat.report.page.statistics.task.service;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.entity.Domain;
import com.dianping.cat.home.service.client.entity.Method;

public class ClientReportStatistics extends BaseVisitor {

	private ClientReport m_clientReport = new ClientReport(Constants.CAT);

	private String m_domain;

	public ClientReport getClienReport() {
		return m_clientReport;
	}

	@Override
	public void visitMachine(Machine machine) {
		if (Constants.ALL.equals(machine.getIp())) {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitName(TransactionName name) {
		Domain domain = m_clientReport.findOrCreateDomain(m_domain);

		domain.incTotalCount(name.getTotalCount());
		domain.incFailureCount(name.getFailCount());
		domain.incSum(name.getSum());
		domain.setFailurePercent(domain.getFailureCount() * 1.0 / domain.getTotalCount());
		domain.setAvg(domain.getSum() / domain.getTotalCount());

		String interf = name.getId();
		Method method = domain.findOrCreateMethod(interf);

		method.incTotalCount(name.getTotalCount());
		method.incFailureCount(name.getFailCount());
		method.incSum(name.getSum());
		method.setFailurePercent(method.getFailureCount() * 1.0 / method.getTotalCount());
		method.setAvg(method.getSum() / method.getTotalCount());
		method.setTimeout(name.getMax());
		super.visitName(name);
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_domain = transactionReport.getDomain();

		m_clientReport.setStartTime(transactionReport.getStartTime());
		m_clientReport.setEndTime(transactionReport.getEndTime());
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		if ("PigeonCall".equals(type.getId()) || "Call".equals(type.getId())) {
			super.visitType(type);
		}
	}

}
