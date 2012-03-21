package com.dianping.cat;

import java.io.File;
import java.net.URL;

import org.codehaus.plexus.PlexusContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.servlet.GzipFilter;
import org.unidal.webres.server.support.SimpleServerSupport;
import org.unidal.webres.taglib.support.JettyTestSupport;

import com.dianping.cat.servlet.CatServlet;
import com.site.lookup.ComponentTestCase;
import com.site.test.browser.BrowserManager;
import com.site.web.MVC;

public class TestServer extends SimpleServerSupport {
	private static ComponentAdaptor s_adaptor = new ComponentAdaptor();

	private static MVC s_mvc = new MVC();

	private static CatServlet s_cat = new CatServlet();

	@AfterClass
	public static void afterClass() throws Exception {
		JettyTestSupport.shutdownServer();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		JettyTestSupport.startServer(new Server());
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server();

		Server.beforeClass();

		try {
			server.before();
			server.startServer();
			server.after();
		} finally {
			Server.shutdownServer();
		}
	}

	@Override
	public void after() {
		super.after();
		s_adaptor.after();
	}

	@Override
	public void before() {
		s_adaptor.setServerPort(getServerPort());
		s_adaptor.before();
		s_mvc.setContainer(s_adaptor.getContainer());
		super.before();
	}

	@Override
	protected String getContextPath() {
		return "/cat";
	}

	@Override
	protected File getScratchDir() {
		File work = new File(System.getProperty("java.io.tmpdir", "."), "Cat");

		work.mkdirs();
		return work;
	}

	@Override
	protected int getServerPort() {
		return 2281;
	}

	@Override
	protected File getWarRoot() {
		return new File("src/main/webapp");
	}

	@Override
	protected void postConfigure(Context ctx) {
		ServletHolder mvc = new ServletHolder(s_mvc);

		mvc.setInitParameter("cat-client-xml", "/data/appdatas/cat/client.xml");
		ctx.addServlet(new ServletHolder(s_cat), "/s/*");
		ctx.addServlet(mvc, "/");
		ctx.addServlet(mvc, "/r/*");
		ctx.addFilter(GzipFilter.class, "/r/*", Handler.ALL);
		super.postConfigure(ctx);
	}

	@Test
	public void startServer() throws Exception {
		// open the page in the default browser
		s_adaptor.display("/cat/r");

		System.out.println(String.format("[%s] Press any key to stop server ... ", getTimestamp()));
		System.in.read();
	}

	static class ComponentAdaptor extends ComponentTestCase {
		private int m_serverPort;

		public void after() {
			try {
				super.tearDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void before() {
			try {
				super.setUp();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void display(String requestUri) throws Exception {
			StringBuilder sb = new StringBuilder(256);
			BrowserManager manager = lookup(BrowserManager.class);

			sb.append("http://localhost:").append(m_serverPort).append(requestUri);

			try {
				manager.display(new URL(sb.toString()));
			} finally {
				release(manager);
			}
		}

		@Override
		public PlexusContainer getContainer() {
			return super.getContainer();
		}

		@Override
		public <T> T lookup(Class<T> role) throws Exception {
			return super.lookup(role);
		}

		@Override
		public <T> T lookup(Class<T> role, Object roleHint) throws Exception {
			return super.lookup(role, roleHint);
		}

		public void setServerPort(int serverPort) {
			m_serverPort = serverPort;
		}
	}
}
