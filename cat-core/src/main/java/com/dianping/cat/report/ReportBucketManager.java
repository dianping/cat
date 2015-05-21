package com.dianping.cat.report;

import java.io.IOException;

public interface ReportBucketManager {

	public void closeBucket(ReportBucket bucket);
	
	public void clearOldReports();

	public ReportBucket getReportBucket(long timestamp, String name ,int index) throws IOException;
}
