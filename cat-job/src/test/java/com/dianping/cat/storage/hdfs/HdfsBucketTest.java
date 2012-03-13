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

		// keys must asc order for offset calculation bellow! 
		final String key1 = "12345678901234567890123456789010";
		final String key2 = "12345678901234567890123456789017";
		final String key3 = "12345678901234567890123456789029";
		final String value1 = "It's A.";
		final String value2 = "It's B.";
		final String value3 = "It's C.";
		final String tagName1 = "t1";

		bucket.batchPut(new BatchHolder() {

			@Override
			public Collection<String> getKeys() {
				return Arrays.asList(new String[] { key1, key2, key3 });
			}

			@Override
			public void getMeta(String key, Meta meta) {
				if (key1.equals(key)) {// tag1
					meta.setOffset(0);
					meta.setLength(value1.length());
					meta.addTag(tagName1);
				} else if (key2.equals(key)) { // tag1
					meta.setOffset(value1.length());
					meta.setLength(value2.length());
					meta.addTag(tagName1);
				} else if (key3.equals(key)) { // no tag
					meta.setOffset(value1.length() + value2.length());
					meta.setLength(value3.length());
				}
			}

			@Override
			public byte[] getValue(String key) {
				byte[] bs = null;
				if (key1.equals(key)) {
					bs = value1.getBytes();
				} else if (key2.equals(key)) {
					bs = value2.getBytes();
				} else if (key3.equals(key)) {
					bs = value3.getBytes();
				}
				return bs;
			}

		});

		bucket.endWrite();
		bucket.startRead();

		Assert.assertNull(bucket.findById("not exist key"));
		Assert.assertEquals(value1, new String(bucket.findById(key1)));
		Assert.assertEquals(value2, new String(bucket.findById(key2)));
		Assert.assertNull(bucket.findNextById(key3, Direction.BACKWARD, tagName1));
		Assert.assertEquals(value2, new String(bucket.findNextById(key1, Direction.BACKWARD, tagName1)));
		Assert.assertEquals(value1, new String(bucket.findNextById(key2, Direction.FORWARD, tagName1)));

		bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.startRead(); // test read

		Assert.assertEquals(value1, new String(bucket.findById(key1)));
		Assert.assertEquals(value2, new String(bucket.findById(key2)));
		Assert.assertEquals(value3, new String(bucket.findById(key3)));
		Assert.assertEquals(value2, new String(bucket.findNextById(key1, Direction.BACKWARD, tagName1)));

		bucket.deleteAndCreate(); // test delete and read

		bucket = (HdfsBucket) manager.getHdfsBucket("/a/b/c");
		bucket.startRead();

		Assert.assertEquals(null, bucket.findById(key1));
		Assert.assertEquals(null, bucket.findById(key2));

		bucket.close();
	}
}
