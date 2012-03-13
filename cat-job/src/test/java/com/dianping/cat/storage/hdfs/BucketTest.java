package com.dianping.cat.storage.hdfs;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class BucketTest extends ComponentTestCase {
	@Test
	public void testBytesBucket() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		Bucket<byte[]> bucket = manager.getHdfsBucket("hdfs");

		bucket.deleteAndCreate();

		// store it and load it
		for (int i = 0; i < 100; i++) {
			String id = "id" + i;
			String t1 = "value" + i;
			String tag = "tag" + (i % 10);
			boolean success = bucket.storeById(id, t1.getBytes(), tag);

			if (success) {
				String t2 = new String(bucket.findById(id));

				Assert.assertEquals("Unable to find data after stored it.", t1, t2);
			} else {
				Assert.fail("Data failed to store at i=" + i + ".");
			}
		}

		// close and reload it, check if everything is okay
		bucket.close();
		bucket.initialize(byte[].class, new File("target/bucket/"), "bytes");

		// store it and load it
		for (int i = 0; i < 100; i++) {
			String id = "id" + i;
			String t1 = "value" + i;
			String t2 = new String(bucket.findById(id));

			Assert.assertEquals("Unable to find data by id.", t1, t2);
		}

		for (int i = 0; i < 90; i++) {
			String id = "id" + i;
			String t1 = "value" + (i + 10);
			String tag = "tag" + (i % 10);
			String t2 = new String(bucket.findNextById(id, tag));

			Assert.assertEquals("Unable to find data by id.", t1, t2);
		}

		for (int i = 10; i < 100; i++) {
			String id = "id" + i;
			String t1 = "value" + (i - 10);
			String tag = "tag" + (i % 10);
			String t2 = new String(bucket.findPreviousById(id, tag));

			Assert.assertEquals("Unable to find data by id.", t1, t2);
		}
	}
}
