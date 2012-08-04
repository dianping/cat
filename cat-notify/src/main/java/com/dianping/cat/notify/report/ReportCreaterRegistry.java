package com.dianping.cat.notify.report;

import java.util.List;

import com.dianping.cat.notify.config.ConfigContext;
import com.dianping.cat.notify.server.ContainerHolder;

public interface ReportCreaterRegistry {
	
	boolean initReportCreaters(ConfigContext configContext,ContainerHolder holder);

	List<ReportCreater> getReportCreaters(String domain);
	
}
