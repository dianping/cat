package com.dianping.cat.storage;

import java.io.IOException;
import java.util.List;

import com.dianping.cat.message.spi.MessageTree;

public interface BucketManager {
	public void closeBucket(Bucket<?> bucket);
	
	public List<Bucket<MessageTree>> getLogviewBuckets(long timestamp,String excludeDomain) throws IOException;

	public Bucket<MessageTree> getLogviewBucket(long timestamp, String domain) throws IOException;

	public Bucket<MessageTree> getMessageBucket(long timestamp, String domain) throws IOException;

	public Bucket<String> getReportBucket(long timestamp, String name) throws IOException;
}
