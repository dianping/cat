package com.dianping.cat.storage;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface BucketManager {
	public void closeBucket(Bucket<?> bucket);

	public Bucket<MessageTree> getMessageBucket(String path) throws IOException;

	public Bucket<String> getStringBucket(String path) throws IOException;

	public Bucket<byte[]> getBytesBucket(String path) throws IOException;
	
	public Bucket<byte[]> getHdfsBucket(String path) throws IOException;
}
