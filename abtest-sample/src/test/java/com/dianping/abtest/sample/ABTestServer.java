package com.dianping.abtest.sample;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.test.jetty.JettyServer;

@RunWith(JUnit4.class)
public class ABTestServer extends JettyServer {
	public static void main(String[] args) throws Exception {
		ABTestServer server = new ABTestServer();

		System.setProperty("devMode", "true");
		server.startServer();
		server.startWebApp();
		server.stopServer();
	}

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
		super.startServer();
	}

	@Override
	protected String getContextPath() {
		return "/abtest-sample";
	}

	@Override
	protected int getServerPort() {
		return 8081;
	}

	@Test
	public void startWebApp() throws Exception {
		// open the page in the default browser
		display("/abtest-sample/index");
		waitForAnyKey();
	}
}
