package com.dianping.cat.hadoop.hdfs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FileSystemManagerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		FileSystemManager manager = lookup(FileSystemManager.class);
		StringBuilder baseDir = new StringBuilder();
		
		System.out.println(manager.getFileSystem("test", baseDir));
		System.out.println(baseDir);
	}
}
