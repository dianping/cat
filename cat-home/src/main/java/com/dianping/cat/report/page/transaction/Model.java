package com.dianping.cat.report.page.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultJsonBuilder;
import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private TransactionReport m_report;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public TransactionReport getReport() {
		return m_report;
	}
	
	public String getReportInJson() {
		DefaultJsonBuilder builder = new DefaultJsonBuilder();
		
		m_report.accept(builder);
		return builder.getString();
	}

	public void setReport(TransactionReport report) {
		m_report = report;
	}
}
