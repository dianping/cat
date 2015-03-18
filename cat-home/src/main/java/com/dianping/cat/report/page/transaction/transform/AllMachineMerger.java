package com.dianping.cat.report.page.transaction.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;

public class AllMachineMerger extends BaseVisitor {

	public TransactionReport m_report;

	public String m_currentType;

	public String m_currentName;

	public Integer m_currentRange;

	public Integer m_currentDuration;

	public TransactionReportMerger m_merger = new TransactionReportMerger(new TransactionReport());

	public TransactionReport getReport() {
		return m_report;
	}

	@Override
	public void visitDuration(Duration duration) {
		m_currentDuration = duration.getValue();
		Duration temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
		      .findOrCreateName(m_currentName).findOrCreateDuration(m_currentDuration);

		m_merger.mergeDuration(temp, duration);

		super.visitDuration(duration);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_report.findOrCreateMachine(Constants.ALL);
		super.visitMachine(machine);
	}

	@Override
	public void visitName(TransactionName name) {
		m_currentName = name.getId();
		TransactionName temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
		      .findOrCreateName(m_currentName);

		m_merger.mergeName(temp, name);
		super.visitName(name);
	}

	@Override
	public void visitRange(Range range) {
		m_currentRange = range.getValue();
		Range temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType)
		      .findOrCreateName(m_currentName).findOrCreateRange(m_currentRange);

		m_merger.mergeRange(temp, range);
		super.visitRange(range);
	}

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		m_report = new TransactionReport(transactionReport.getDomain());
		m_report.setStartTime(transactionReport.getStartTime());
		m_report.setEndTime(transactionReport.getEndTime());
		m_report.getDomainNames().addAll(transactionReport.getDomainNames());
		m_report.getIps().addAll(transactionReport.getIps());

		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitType(TransactionType type) {
		m_currentType = type.getId();
		TransactionType temp = m_report.findOrCreateMachine(Constants.ALL).findOrCreateType(m_currentType);

		m_merger.mergeType(temp, type);
		super.visitType(type);
	}

}
