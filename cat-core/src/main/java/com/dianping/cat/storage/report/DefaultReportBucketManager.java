package com.dianping.cat.storage.report;

import java.io.IOException;
import java.util.Date;

import org.unidal.lookup.ContainerHolder;

public class DefaultReportBucketManager extends ContainerHolder implements ReportBucketManager {

	@Override
	public void closeBucket(ReportBucket<?> bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
		} finally {
			release(bucket);
		}
	}

	private ReportBucket<?> createBucket(Class<?> type, Date timestamp, String name, String namespace) throws IOException {
		ReportBucket<?> bucket = lookup(ReportBucket.class, type.getName() + "-" + namespace);

		bucket.initialize(type, name, timestamp);
		return bucket;
	}

	@SuppressWarnings("unchecked")
	private <T> ReportBucket<T> getBucket(Class<T> type, long timestamp, String name, String namespace) throws IOException {
		Date date = new Date(timestamp);
		ReportBucket<?> bucket = createBucket(type, date, name, namespace);

		return (ReportBucket<T>) bucket;
	}

	@Override
	public ReportBucket<String> getReportBucket(long timestamp, String name) throws IOException {
		return getBucket(String.class, timestamp, name, "report");
	}

}
