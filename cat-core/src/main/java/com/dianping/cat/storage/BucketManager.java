package com.dianping.cat.storage;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface BucketManager {
	public void closeBucket(Bucket<?> bucket);

	public Bucket<MessageTree> getLogviewBucket(long timestamp, String domain) throws IOException;

	public Bucket<MessageTree> getMessageBucket(long timestamp, String domain) throws IOException;

	public Bucket<String> getReportBucket(long timestamp, String name) throws IOException;
}
