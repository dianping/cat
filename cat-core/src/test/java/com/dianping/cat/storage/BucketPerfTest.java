package com.dianping.cat.storage;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class BucketPerfTest extends ComponentTestCase {
	private int perfTimes = 10000;

	/**
	 * Test method for {@link com.dianping.tkv.TkvImpl#get(java.lang.String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPutDiffKeyWithoutTagsPerf() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		Bucket<byte[]> bucket = manager.getBytesBucket("perf1");
		String value = "0123456789";
		long start = System.currentTimeMillis();

		for (int i = 0; i < perfTimes; i++) {
			bucket.storeById(String.valueOf(10000000 + i), value.getBytes());
		}

		System.out.println("testPutDiffKeyWithoutTagsPerf:" + (System.currentTimeMillis() - start));
	}

	@Test
	public void testGetDiffKeyWithoutTagsPerf() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		Bucket<byte[]> bucket = manager.getBytesBucket("perf2");
		String value = "0123456789";

		for (int i = 0; i < perfTimes; i++) {
			bucket.storeById(String.valueOf(10000000 + i), value.getBytes());
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < perfTimes; i++) {
			bucket.findById(String.valueOf(10000000 + i));
		}

		System.out.println("testGetDiffKeyWithoutTagsPerf:" + (System.currentTimeMillis() - start));
	}

	@Test
	public void testGetTagRecordPerf() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		Bucket<byte[]> bucket = manager.getBytesBucket("perf2");
		String value = "0123456789";

		for (int i = 0; i < perfTimes; i++) {
			bucket.storeById(String.valueOf(10000000 + i), value.getBytes(), "pet" + (i % 1000));
		}

		long start = System.currentTimeMillis();
		for (int i = 0; i < perfTimes; i++) {
			bucket.findNextById(String.valueOf(10000000 + i), "pet" + (i % 1000));
		}

		System.out.println("testGetTagRecordPerf:" + (System.currentTimeMillis() - start));
	}
}
