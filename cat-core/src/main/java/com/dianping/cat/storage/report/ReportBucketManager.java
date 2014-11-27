package com.dianping.cat.storage.report;

import java.io.IOException;

public interface ReportBucketManager {

	public void closeBucket(ReportBucket<?> bucket);

	public ReportBucket<String> getReportBucket(long timestamp, String name) throws IOException;
}
