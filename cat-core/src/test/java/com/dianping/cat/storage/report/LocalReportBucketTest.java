package com.dianping.cat.storage.report;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.After;

import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.StringBucketTestCase;

public class LocalReportBucketTest extends StringBucketTestCase {

	@SuppressWarnings("unchecked")
	protected Bucket<String> createBucket() throws Exception, IOException {
		Bucket<String> bucket = lookup(Bucket.class, String.class.getName() + "-report");
		bucket.initialize(null, "cat", new Date());
		return bucket;
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		String m_baseDir = ((LocalReportBucket) this.bucket).getBaseDir();
		String logicalPath = ((LocalReportBucket) this.bucket).getLogicalPath();
		new File(m_baseDir, logicalPath).delete();
		new File(m_baseDir, logicalPath + ".idx").delete();
	}

}
