package com.dianping.cat.abtest.demo.roundrobin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.unidal.test.jetty.JettyServer;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;

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
		return "/abtest";
	}

	@Override
	protected int getServerPort() {
		return 8081;
	}

	@Override
	protected boolean isWebXmlDefined() {
		return false;
	}

	@Override
	protected void postConfigure(WebAppContext context) {
		context.addFilter(CatFilter.class, "/*", Handler.REQUEST | Handler.FORWARD);
		context.addServlet(new ServletHolder(SimpleRoundRobinServlet.class), "/roundrobin/*");
	}

	@Override
	protected void setupContainer() throws Exception {
		super.setupContainer();

		Cat.initialize(getContainer(), null);
	}

	@Test
	public void startWebApp() throws Exception {
		// open the page in the default browser
		display("/abtest/roundrobin");
		waitForAnyKey();
	}
}
