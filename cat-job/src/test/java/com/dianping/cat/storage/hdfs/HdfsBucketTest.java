package com.dianping.cat.storage.hdfs;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.TagThreadSupport.Direction;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HdfsBucketTest extends ComponentTestCase {
	@Test
	public void testLookup() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		Bucket<byte[]> bucket = manager.getHdfsBucket("/a/b/c");

		bucket.storeById("a", "It's A.".getBytes(), "t1");
		bucket.storeById("b", "It's B.".getBytes(), "t1");

		Assert.assertEquals("It's A.", new String(bucket.findById("a")));
		Assert.assertEquals("It's B.", new String(bucket.findById("b")));
		Assert.assertEquals("It's B.", new String(bucket.findNextById("a", Direction.BACKWARD, "t1")));

		bucket.close();

		bucket = manager.getHdfsBucket("/a/b/c");

		Assert.assertEquals("It's A.", new String(bucket.findById("a")));
		Assert.assertEquals("It's B.", new String(bucket.findById("b")));
		Assert.assertEquals("It's B.", new String(bucket.findNextById("a", Direction.BACKWARD, "t1")));

		bucket.deleteAndCreate();

		bucket = manager.getHdfsBucket("/a/b/c");

		Assert.assertEquals(null, bucket.findById("a"));
		Assert.assertEquals(null, bucket.findById("b"));

	}
}
