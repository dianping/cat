package com.dianping.cat.storage;

import java.io.IOException;
import java.util.Date;

import com.dianping.cat.message.spi.MessageTree;

public interface BucketManager {
	public void closeBucket(Bucket<?> bucket);

	public Bucket<MessageTree> getMessageBucket(Date timestamp, String domain) throws IOException;

	public Bucket<String> getReportBucket(Date timestamp, String name) throws IOException;
}
