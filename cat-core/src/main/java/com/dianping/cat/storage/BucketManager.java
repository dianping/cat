package com.dianping.cat.storage;

import java.io.IOException;

public interface BucketManager {

	public void closeBucket(Bucket<?> bucket);

	public Bucket<String> getReportBucket(long timestamp, String name) throws IOException;
}
