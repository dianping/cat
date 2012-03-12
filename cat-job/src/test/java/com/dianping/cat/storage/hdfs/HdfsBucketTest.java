package com.dianping.cat.storage.hdfs;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.hdfs.HdfsBucket;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.storage.TagThreadSupport.Direction;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HdfsBucketTest extends ComponentTestCase {

	@Test
	public void testLookup() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		HdfsBucket bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.deleteAndCreate();

		bucket.startWrite();

		// key must asc sort!
		final String key1 = "12345678901234567890123456789011";
		final String key2 = "12345678901234567890123456789012";
		final String value1 = "It's A.";
		final String value2 = "It's B.";
		final String tagName1 = "t1";

		bucket.batchPut(new BatchHolder() {

			@Override
			public Collection<String> getKeys() {
				return Arrays.asList(new String[] { key1, key2 });
			}

			@Override
			public void getMeta(String key, Meta meta) {
				if (key1.equals(key)) {
					meta.setOffset(0);
					meta.setLength(value1.length());
					meta.addTag(tagName1);
				} else if (key2.equals(key)) {
					meta.setOffset(value1.length());
					meta.setLength(value2.length());
					meta.addTag(tagName1);
				}
			}

			@Override
			public byte[] getValue(String key) {
				byte[] bs = null;
				if (key1.equals(key)) {
					bs = value1.getBytes();
				} else if (key2.equals(key)) {
					bs = value2.getBytes();
				}
				return bs;
			}

		});

		bucket.endWrite();
		bucket.startRead();

		Assert.assertEquals(value1, new String(bucket.findById(key1)));
		Assert.assertEquals(value2, new String(bucket.findById(key2)));
		Assert.assertEquals(value2, new String(bucket.findNextById(key1, Direction.BACKWARD, tagName1)));

		bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.startRead();

		Assert.assertEquals(value1, new String(bucket.findById(key1)));
		Assert.assertEquals(value2, new String(bucket.findById(key2)));
		Assert.assertEquals(value2, new String(bucket.findNextById(key1, Direction.BACKWARD, tagName1)));

		bucket.deleteAndCreate();

		bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.startRead();

		Assert.assertEquals(null, bucket.findById(key1));
		Assert.assertEquals(null, bucket.findById(key2));

		bucket.close();
	}
}
