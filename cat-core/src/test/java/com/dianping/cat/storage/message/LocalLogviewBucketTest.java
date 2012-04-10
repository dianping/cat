package com.dianping.cat.storage.message;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.MesageTreeBucketTestCase;

@RunWith(JUnit4.class)
public class LocalLogviewBucketTest extends MesageTreeBucketTestCase {

	@SuppressWarnings("unchecked")
	protected Bucket<MessageTree> createBucket() throws Exception, IOException {
		Bucket<MessageTree> bucket = lookup(Bucket.class, MessageTree.class.getName() + "-logview");
		bucket.initialize(null, "cat", new Date());
		return bucket;
	}
	
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		String m_baseDir = "target/bucket";
		String logicalPath = ((LocalLogviewBucket) this.bucket).getLogicalPath();
		new File(m_baseDir, logicalPath).delete();
		new File(m_baseDir, logicalPath + ".idx").delete();
	}
}
