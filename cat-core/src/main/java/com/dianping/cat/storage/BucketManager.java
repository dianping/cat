package com.dianping.cat.storage;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface BucketManager {
	public void closeBucket(Bucket<?> bucket);

	public Bucket<MessageTree> getMessageBucket(String path) throws IOException;

	public Bucket<String> getReportBucket(String path) throws IOException;
}
