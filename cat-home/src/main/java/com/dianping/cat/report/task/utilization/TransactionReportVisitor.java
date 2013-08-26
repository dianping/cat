package com.dianping.cat.report.task.utilization;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.UtilizationReport;

public class TransactionReportVisitor extends BaseVisitor {

	private String m_domain;

	private UtilizationReport m_report;

	public TransactionReportVisitor setReport(UtilizationReport report) {
		m_report = report;
		return this;
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();
		if (CatString.ALL.equals(ip)) {
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
		String key = type.getId();
		Domain domain = m_report.findOrCreateDomain(m_domain);
		
		if (key.indexOf("URL") >= 0) {
			domain.setUrlCount(type.getTotalCount());
			domain.setUrlResponseTime(type.getAvg());
		} else if (key.indexOf("Service") >= 0 || key.indexOf("PigeonService") >= 0) {
			domain.setServiceCount(type.getTotalCount());
			domain.setServiceResponseTime(type.getAvg());
		} else if (key.indexOf("SQL") >= 0) {
			domain.setSqlCount(type.getTotalCount());
		} else if (key.indexOf("Cache.memcached") >= 0) {
			domain.setMemcacheCount(type.getTotalCount() + domain.getMemcacheCount());
		} else if (key.indexOf("SwallowHeartbeat") >= 0) {
			domain.setSwallowCallCount(type.getTotalCount());
		} else if (key.indexOf("PigeonCall") >= 0 || key.indexOf("Call") >= 0) {
			domain.setPigeonCallCount(type.getTotalCount());
		}
	}

}
