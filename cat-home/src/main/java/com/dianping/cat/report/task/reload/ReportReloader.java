package com.dianping.cat.report.task.reload;

import java.util.List;

public interface ReportReloader {

	public List<ReportReloadEntity> loadReport(long time);

	public String getId();

	public boolean reload(long time);

}
