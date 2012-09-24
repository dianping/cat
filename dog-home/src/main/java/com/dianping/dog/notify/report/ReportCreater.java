package com.dianping.dog.notify.report;

import org.codehaus.plexus.logging.LogEnabled;


public interface ReportCreater extends LogEnabled {

	boolean init(ReportConfig config, DefaultContainerHolder holder);

	boolean isNeedToCreate(long timestamp);

	String createReport(long timestamp,String domain);

}