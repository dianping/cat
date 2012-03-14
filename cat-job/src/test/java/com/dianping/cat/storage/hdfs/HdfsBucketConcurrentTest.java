package com.dianping.cat.storage.hdfs;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.hdfs.HdfsBucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HdfsBucketConcurrentTest extends ComponentTestCase {

	@Test
	public void testConcurrentWriteRead() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		final HdfsBucket bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.deleteAndCreate();
		bucket.startWrite();

		ExecutorService pool = Executors.newFixedThreadPool(10);

		long start = System.currentTimeMillis();
		for (int p = 0; p < 10; p++) {
			final int num = p;

			pool.submit(new Runnable() {
				public void run() {
					try {
						for (int i = 0; i < 100; i++) {
							int seq = num * 100 + i;
							String id = "id" + seq;
							String t1 = "value" + seq;
							boolean success = bucket.storeById(id, t1.getBytes());

							if (!success) {
								Assert.fail("Data failed to store at " + seq + ".");
							}
						}
					} catch (IOException e) {
						Assert.fail(e.getMessage());
					}
				}
			});
		}

		pool.awaitTermination(5000, TimeUnit.MILLISECONDS);
		System.out.println("finished concurrent write." + (System.currentTimeMillis() - start));

		bucket.flush();

		pool = Executors.newFixedThreadPool(10);

		start = System.currentTimeMillis();
		for (int p = 0; p < 10; p++) {
			final int num = p;

			pool.submit(new Runnable() {
				public void run() {
					try {
						for (int i = 0; i < 100; i++) {
							int seq = num * 100 + i;
							String id = "id" + seq;
							String t1 = "value" + seq;
							String t2 = new String(bucket.findById(id));
							Assert.assertEquals("Unable to find data after stored it." + id, t1, t2);
						}
					} catch (IOException e) {
						Assert.fail(e.getMessage());
					}
				}
			});
		}

		pool.awaitTermination(5000, TimeUnit.MILLISECONDS);

		System.out.println("finished concurrent read." + (System.currentTimeMillis() - start));

		final HdfsBucket bucket2 = (HdfsBucket) manager.getHdfsBucket("/a/b/c");

		// test serial read
		start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			String id = "id" + i;
			String t1 = "value" + i;
			byte[] value = bucket2.findById(id);
			if(value == null || value.length == 0) {
				System.out.println(id);
			}
			String t2 = new String(value);
			Assert.assertEquals("Unable to find data after stored it." + id, t1, t2);
		}
		System.out.println("finished serial read." + (System.currentTimeMillis() - start));

		bucket2.close();

		bucket2.deleteAndCreate();
		
		bucket2.delete();
	}

}
