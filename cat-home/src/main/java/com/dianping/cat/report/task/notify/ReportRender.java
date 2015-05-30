package com.dianping.cat.report.task.notify;

import com.dianping.cat.event.model.entity.EventReport;
import com.dianping.cat.problem.model.entity.ProblemReport;
import com.dianping.cat.transaction.model.entity.TransactionReport;

public interface ReportRender {

	public String renderReport(EventReport report);

	public String renderReport(ProblemReport report);

	public String renderReport(TransactionReport report);
}
