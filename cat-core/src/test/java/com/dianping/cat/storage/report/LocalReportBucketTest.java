package com.dianping.cat.storage.report;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.After;

import com.dianping.cat.report.LocalReportBucket;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.storage.StringBucketTestCase;

public class LocalReportBucketTest extends StringBucketTestCase {

	@Override
	protected ReportBucket createBucket() throws Exception, IOException {
		ReportBucket bucket = lookup(ReportBucket.class, String.class.getName() + "-report");
		bucket.initialize("cat", new Date(), 0);
		return bucket;
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		String m_baseDir = ((LocalReportBucket) this.bucket).getBaseDir();
		String logicalPath = ((LocalReportBucket) this.bucket).getLogicalPath();
		new File(m_baseDir, logicalPath).delete();
		new File(m_baseDir, logicalPath + ".idx").delete();
	}

}
