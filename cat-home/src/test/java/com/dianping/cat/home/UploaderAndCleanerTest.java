package com.dianping.cat.home;

import java.io.File;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.UploaderAndCleaner;

public class UploaderAndCleanerTest extends ComponentTestCase{

	@Test
	public void test() throws Exception{
		ServerConfigManager serverConfigManager = lookup(ServerConfigManager.class);

		serverConfigManager.initialize(new File("/data/appdatas/cat/server.xml"));

		UploaderAndCleaner task = lookup(UploaderAndCleaner.class);
		
		task.deleteOldReports();
	}
}
