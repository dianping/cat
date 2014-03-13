package com.dianping.cat.hadoop.hdfs;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FileSystemManagerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		FileSystemManager manager = lookup(FileSystemManager.class);
		StringBuilder baseDir = new StringBuilder();

		Assert.assertNotNull(manager.getFileSystem("test", baseDir));
		Assert.assertEquals("target/bucket/hdfs/test", baseDir.toString());
	}
}
