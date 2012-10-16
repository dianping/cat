package com.dianping.cat.system.notify;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public interface ReportRender {

	public String renderReport(EventReport report);

	public String renderReport(HealthReport report);

	public String renderReport(ProblemReport report);

	public String renderReport(TransactionReport report);
}
