package com.dianping.cat.report;

import java.io.IOException;

public interface ReportBucketManager {

	public void closeBucket(ReportBucket<?> bucket);
	
	public void clearOldReports();

	public ReportBucket<String> getReportBucket(long timestamp, String name) throws IOException;
}
