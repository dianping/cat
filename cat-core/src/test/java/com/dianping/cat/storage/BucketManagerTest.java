package com.dianping.cat.storage;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class BucketManagerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		Date timestamp = new Date();
		BucketManager manager = lookup(BucketManager.class);
		Bucket<MessageTree> bucket1 = manager.getMessageBucket(timestamp, "test/path1");
		Bucket<MessageTree> bucket2 = manager.getMessageBucket(timestamp, "test/path2");
		Bucket<MessageTree> bucket3 = manager.getMessageBucket(timestamp, "test/path1");
		Bucket<MessageTree> bucket4 = manager.getMessageBucket(timestamp, "test/path2");

		Assert.assertEquals(bucket1, bucket3);
		Assert.assertEquals(bucket2, bucket4);
	}
}
