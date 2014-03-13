package com.dianping.cat.storage;

import java.io.IOException;
import java.util.Date;

import org.unidal.lookup.ContainerHolder;

public class DefaultBucketManager extends ContainerHolder implements BucketManager {

	@Override
	public void closeBucket(Bucket<?> bucket) {
		try {
			bucket.close();
		} catch (Exception e) {
			// ignore it
		}
		release(bucket);
	}

	protected Bucket<?> createBucket(Class<?> type, Date timestamp, String name, String namespace) throws IOException {
		Bucket<?> bucket = lookup(Bucket.class, type.getName() + "-" + namespace);

		bucket.initialize(type, name, timestamp);
		return bucket;
	}

	@SuppressWarnings("unchecked")
	protected <T> Bucket<T> getBucket(Class<T> type, long timestamp, String name, String namespace) throws IOException {
		Date date = new Date(timestamp);
		Bucket<?> bucket = createBucket(type, date, name, namespace);

		return (Bucket<T>) bucket;
	}

	@Override
	public Bucket<String> getReportBucket(long timestamp, String name) throws IOException {
		return getBucket(String.class, timestamp, name, "report");
	}

}
