package com.dianping.cat.storage.hdfs;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.hdfs.HdfsBucket;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class BucketTest extends ComponentTestCase {
	@Test
	public void testBytesBucket() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		Bucket<byte[]> bucket = manager.getHdfsBucket("hdfs");
		((HdfsBucket)bucket).delete();
		bucket.deleteAndCreate();
		
		((HdfsBucket)bucket).startWrite();

		// store it
		for (int i = 0; i < 100; i++) {
			String id = "id" + i;
			String t1 = "value" + i;
			String tag = "tag" + (i % 10);
			boolean success = bucket.storeById(id, t1.getBytes(), tag);

			if (success) {
			} else {
				Assert.fail("Data failed to store at i=" + i + ".");
			}
		}

		bucket.flush();

		// load it
		for (int i = 0; i < 100; i++) {
			String id = "id" + i;
			String t1 = "value" + i;
			String tag = "tag" + (i % 10);
			String t2 = new String(bucket.findById(id));
			Assert.assertEquals("Unable to find data after stored it.", t1, t2);
		}

		// close and reload it, check if everything is okay
		bucket.close();
		((HdfsBucket)bucket).delete();
	}
	
	public static void main(String[] args) {
		System.out.println(Integer.toHexString(-1));
		System.out.println(Integer.toHexString(1));
	}
}
