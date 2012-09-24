package com.dianping.dog.notify.report;

import java.util.List;

import com.dianping.dog.notify.config.ConfigContext;

public interface ReportCreaterRegistry {
	
	boolean initReportCreaters(ConfigContext configContext,DefaultContainerHolder holder);

	List<ReportCreater> getReportCreaters(String domain);
	
}
