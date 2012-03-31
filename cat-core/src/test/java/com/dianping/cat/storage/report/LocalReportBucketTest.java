package com.dianping.cat.storage.report;

import java.io.IOException;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.StringBucketTestCase;

public class LocalReportBucketTest extends StringBucketTestCase {

	@SuppressWarnings("unchecked")
	protected Bucket<String> createBucket() throws Exception, IOException {
		Bucket<String> bucket = lookup(Bucket.class, String.class.getName() + "-report");
		bucket.initialize(null, "cat", new Date());
		return bucket;
	}
	
	@Test
	@Ignore
	public void testReload() throws Exception {
	}
}
