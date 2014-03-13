package com.dianping.cat;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;

public class ServerConfigManagerTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		ServerConfigManager manager = lookup(ServerConfigManager.class);
		String path = System.getProperty("user.dir") + "/src/test/resources/com/dianping/cat/server.xml";
		File file = new File(path);

		manager.initialize(file);
		manager.initialize();
		String id = "logview";
		Assert.assertEquals(true, manager.getServerConfig() != null);
		Assert.assertEquals(null, manager.getBindHost());
		Assert.assertEquals(2280, manager.getBindPort());
		Assert.assertEquals("Cat", manager.getConsoleDefaultDomain());
		Assert.assertEquals("[Pair[key=127.0.0.1, value=2281]]", manager.getConsoleEndpoints().toString());
		Assert.assertEquals("127.0.0.1:2281", manager.getConsoleRemoteServers());
		Assert.assertEquals("logview", manager.getHdfsBaseDir(id));
		Assert.assertEquals(134217728, manager.getHdfsFileMaxSize(id));
		Assert.assertEquals("target/bucket/logview", manager.getHdfsLocalBaseDir(id));
		Assert.assertEquals("hdfs://10.1.77.86/user/cat", manager.getHdfsServerUri(id));
		Assert.assertEquals(6, manager.getHdfsProperties().size());
		Assert.assertEquals(0, manager.getLongConfigDomains().size());
		Assert.assertEquals(1000, manager.getLongSqlDefaultThreshold());
		Assert.assertEquals(100, manager.getLongUrlDefaultThreshold());
		Assert.assertEquals("target/bucket", manager.getStorageLocalBaseDir());
		Assert.assertEquals(true, manager.isClientCall("Call"));
		Assert.assertEquals(true, manager.isHdfsOn());
		Assert.assertEquals(true, manager.isInitialized());
		Assert.assertEquals(false, manager.isJobMachine());
		Assert.assertEquals(false, manager.isLocalMode());
		Assert.assertEquals(true, manager.isServerService("Service"));
		Assert.assertEquals(false, manager.validateDomain("All"));
		
		Transaction t = new DefaultTransaction("Service", "piegonService:heartTaskService:heartBeat", null);
		Assert.assertEquals(true, manager.discardTransaction(t));

		manager.initialize(null);

		Assert.assertEquals(true, manager.getServerConfig() != null);
	}
}
