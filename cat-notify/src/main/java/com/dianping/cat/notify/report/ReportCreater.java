package com.dianping.cat.notify.report;

import com.dianping.cat.notify.server.ContainerHolder;

public interface ReportCreater {

	boolean init(ReportConfig config, ContainerHolder holder);

	boolean isNeedToCreate(long timestamp);

	String createReport(long timestamp,String domain);

}