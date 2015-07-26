package com.dianping.cat.report.page.top;

import java.text.SimpleDateFormat;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.report.page.top.DomainInfo.Metric;

public class TransactionReportVisitor extends BaseVisitor {

	private DomainInfo m_info;

	private String m_ipAddress;

	private String m_type;

	private String m_date;

	@Override
	public void visitTransactionReport(TransactionReport transactionReport) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:");

		m_date = sdf.format(transactionReport.getStartTime());
		super.visitTransactionReport(transactionReport);
	}

	@Override
	public void visitMachine(Machine machine) {
		String id = machine.getIp();

		if (Constants.ALL.equals(m_ipAddress) || id.equals(m_ipAddress)) {
			super.visitMachine(machine);
		}
	}

	public TransactionReportVisitor(String ipAddress, DomainInfo info, String type) {
		m_info = info;
		m_type = type;
		m_ipAddress = ipAddress;
	}

	@Override
	public void visitType(TransactionType type) {
		if (type.getId().equals(m_type)) {
			super.visitType(type);
		}
	}

	@Override
	public void visitRange(Range range) {
		int minute = range.getValue();
		String key = "";

		if (minute >= 10) {
			key = m_date + minute;
		} else {
			key = m_date + '0' + minute;
		}
		Metric metric = m_info.getMetric(key);

		metric.get(m_type).setFail(range.getFails()).setValue(range.getCount(), range.getSum());
	}
}