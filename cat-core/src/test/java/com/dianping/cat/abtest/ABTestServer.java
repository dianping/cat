package com.dianping.cat.abtest;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.unidal.test.jetty.JettyServer;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;

@RunWith(JUnit4.class)
public class ABTestServer extends JettyServer {

	@Test
	public void startWebApp() throws Exception {
		File file = new File("/data/appdatas/cat/client.xml");
		Cat.initialize(file);

		Server server = new Server(getServerPort());
		Context root = new Context(server, "/", Context.SESSIONS);
		root.addFilter(CatFilter.class, "/*", Handler.REQUEST | Handler.FORWARD);
		root.addServlet(new ServletHolder(SimpleRoundRobinWebPage.class), "/abtest");
		server.start();
		// open the page in the default browser
		display("/abtest");
		waitForAnyKey();
	}

	@Override
	protected int getServerPort() {
		return 8081;
	}

	@Override
	protected String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

}
