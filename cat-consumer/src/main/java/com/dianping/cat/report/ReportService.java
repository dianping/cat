package com.dianping.cat.report;

import com.dianping.cat.report.model.ModelRequest;

public interface ReportService<T> {
	public T getHouylyReport(ModelRequest request);

	public T getDailyReport(ModelRequest request);

	public T getWeeklyReport(ModelRequest request);

	public T getMonthlyReport(ModelRequest request);
}
