package com.dianping.cat.abtest.demo.roundrobin;

import java.io.File;
import java.util.HashMap;

import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.unidal.lookup.ContainerLoader;
import org.unidal.test.jetty.JettyServer;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;

@RunWith(JUnit4.class)
public class ABTestServer extends JettyServer {
	public static void main(String[] args) throws Exception {
		ABTestServer server = new ABTestServer();
		System.setProperty("devMode", "true");
		server.setupContainer();
		server.startServer();
		server.startWebApp();
		server.stopServer();
	}

	@Override
	protected File getWarRoot() {
		String path = getClass().getResource("/abtest/roundrobin").getPath();

		return new File(path);
	}

	@Before
	public void before() throws Exception {
		setupContainer();

		System.setProperty("devMode", "true");
		super.startServer();
	}

	private void setupContainer() {
		DefaultContainerConfiguration configuration = new DefaultContainerConfiguration();
		String defaultConfigurationName = getClass().getName().replace('.', '/') + ".xml";

		configuration.setName("test").setContext(new HashMap<Object, Object>());
		configuration.setContainerConfiguration(defaultConfigurationName);
		PlexusContainer container = ContainerLoader.getDefaultContainer(configuration);

		Cat.initialize(container, null);
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
	protected void postConfigure(WebAppContext context) {
		 context.addFilter(CatFilter.class, "/*", Handler.REQUEST | Handler.FORWARD);
		 context.addServlet(new ServletHolder(SimpleRoundRobinServlet.class), "/roundrobin/*");
	}

	@Test
	public void startWebApp() throws Exception {
		// open the page in the default browser
		display("/abtest/roundrobin");
		waitForAnyKey();
	}
}
