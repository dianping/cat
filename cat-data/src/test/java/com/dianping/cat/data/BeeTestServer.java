package com.dianping.cat.data;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.alibaba.cobar.CobarServer;
import com.dianping.bee.server.SimpleServer;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class BeeTestServer extends ComponentTestCase {

	@Test
	public void runServer() throws Exception {
		SimpleServer server = lookup(SimpleServer.class);

		server.startup();

		System.out.println(CobarServer.getInstance().getConfig().getUsers());

		System.out.println("Press any key to continue ...");
		System.in.read();
	}

}
