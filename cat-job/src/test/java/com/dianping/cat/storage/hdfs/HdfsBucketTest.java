package com.dianping.cat.storage.hdfs;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.hdfs.HdfsBucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HdfsBucketTest extends ComponentTestCase {

	@Test
	public void testLookup() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		HdfsBucket bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.delete();
		bucket.deleteAndCreate();
		bucket.startWrite();

		// keys must asc order for offset calculation bellow!
		final String key1 = "12345678901234567890123456789010";
		final String key2 = "12345678901234567890123456789017";
		final String key3 = "12345678901234567890123456789029";
		final String value1 = "It's A.";
		final String value2 = "It's B.";
		final String value3 = "It's C.";
		final String tagName1 = "t1";

		bucket.storeById(key1, value1.getBytes(), tagName1);
		bucket.storeById(key2, value2.getBytes(), tagName1);
		bucket.storeById(key3, value3.getBytes());

		bucket.flush();

		Assert.assertNull(bucket.findById("not exist key"));
		Assert.assertEquals(value1, new String(bucket.findById(key1)));
		Assert.assertEquals(value2, new String(bucket.findById(key2)));
		Assert.assertNull(bucket.findNextById(key3, tagName1));
		Assert.assertNull(bucket.findPreviousById(key3, tagName1));
		Assert.assertEquals(value2, new String(bucket.findNextById(key1, tagName1)));
		Assert.assertEquals(value1, new String(bucket.findPreviousById(key2, tagName1)));

		bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c"); // test read

		Assert.assertEquals(value1, new String(bucket.findById(key1)));
		Assert.assertEquals(value2, new String(bucket.findById(key2)));
		Assert.assertEquals(value3, new String(bucket.findById(key3)));
		Assert.assertEquals(value2, new String(bucket.findNextById(key1, tagName1)));
		Assert.assertEquals(value1, new String(bucket.findPreviousById(key2, tagName1)));

		
		bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.deleteAndCreate();
		bucket.startRead();

		Assert.assertEquals(value1, new String(bucket.findById(key1)));
		Assert.assertEquals(value2, new String(bucket.findById(key2)));

		bucket.close();
		bucket.delete();
	}
}
