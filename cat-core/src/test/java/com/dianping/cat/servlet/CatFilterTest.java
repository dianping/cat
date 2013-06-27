package com.dianping.cat.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.unidal.helper.Files;
import org.unidal.helper.Joiners;
import org.unidal.helper.Urls;
import org.unidal.test.jetty.JettyServer;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class CatFilterTest extends JettyServer {
	@After
	public void after() throws Exception {
		super.stopServer();
	}

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
		super.startServer();
	}

	@Override
	protected String getContextPath() {
		return "/mock";
	}

	@Override
	protected int getServerPort() {
		return 2282;
	}

	@Override
	protected boolean isWebXmlDefined() {
		return false;
	}

	@Override
	protected void postConfigure(WebAppContext context) {
		context.addServlet(MockServlet.class, "/*");
		context.addFilter(MockABTestFilter.class, "/*", Handler.REQUEST);
		context.addFilter(CatFilter.class, "/*", Handler.REQUEST);
	}

	@Test
	public void testForABTest() throws Exception {
		String url = "http://localhost:2282/mock/abtest?metricType=mockMetric";
		InputStream in = Urls.forIO().openStream(url);
		String content = Files.forIO().readFrom(in, "utf-8");

		Assert.assertEquals("mock content here!", content);

		TimeUnit.MILLISECONDS.sleep(100);
	}

	@Test
	public void testMode0() throws Exception {
		String url = "http://localhost:2282/mock/mode0";
		InputStream in = Urls.forIO().openStream(url);
		String content = Files.forIO().readFrom(in, "utf-8");

		Assert.assertEquals("mock content here!", content);

		TimeUnit.MILLISECONDS.sleep(100);
	}

	@Test
	public void testMode1() throws Exception {
		String url = "http://localhost:2282/mock/mode1";
		Transaction t = Cat.newTransaction("Mock", "testMode1");

		try {
			String childId = Cat.createMessageId();
			String id = Cat.getManager().getThreadLocalMessageTree().getMessageId();

			Cat.logEvent("RemoteCall", url, Message.SUCCESS, childId);

			InputStream in = Urls.forIO().connectTimeout(100) //
			      .header("X-Cat-Id", childId) //
			      .header("X-Cat-Parent-Id", id) //
			      .header("X-Cat-Root-Id", id) //
			      .openStream(url);
			String content = Files.forIO().readFrom(in, "utf-8");

			Assert.assertEquals("mock content here!", content);

			t.setStatus(Message.SUCCESS);
		} finally {
			t.complete();
		}

		TimeUnit.MILLISECONDS.sleep(100);
	}

	@Test
	public void testMode2() throws Exception {
		String url = "http://localhost:2282/mock/mode2";
		Map<String, List<String>> headers = new HashMap<String, List<String>>();
		InputStream in = Urls.forIO().connectTimeout(100) //
		      .header("X-Cat-Source", "container") //
		      .openStream(url, headers);
		String content = Files.forIO().readFrom(in, "utf-8");

		Assert.assertEquals("mock content here!", content);

		String id = getHeader(headers, "X-CAT-ID");
		String parentId = getHeader(headers, "X-CAT-PARENT-ID");
		String rootId = getHeader(headers, "X-CAT-ROOT-ID");

		Assert.assertNotNull(id);
		Assert.assertNotNull(parentId);
		Assert.assertNotNull(rootId);

		Assert.assertFalse(id.equals(rootId));

		Cat.setup(null);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		tree.setMessageId(rootId);

		Transaction t = Cat.newTransaction("Mock", "testMode2");
		Cat.logEvent("RemoteCall", url, Message.SUCCESS, id);
		t.setStatus(Message.SUCCESS);
		t.complete();

		TimeUnit.MILLISECONDS.sleep(100);
	}

	private String getHeader(Map<String, List<String>> headers, String name) {
		List<String> values = headers.get(name);
		int len = values.size();

		if (len == 0) {
			return null;
		} else if (len == 1) {
			return values.get(0);
		} else {
			return Joiners.by(',').join(values);
		}
	}

	public static class MockABTestFilter implements Filter {
		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
		      ServletException {
			String metricType = ((HttpServletRequest) request).getParameter("metricType");

			if (metricType != null) {
				DefaultMessageManager manager = (DefaultMessageManager) Cat.getManager();

				manager.setMetricType(metricType);
			}
			
			chain.doFilter(request, response);
		}

		@Override
		public void destroy() {
		}
	}

	public static class MockServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
			PrintWriter writer = res.getWriter();
			Transaction t = Cat.newTransaction("Mock", req.getRequestURI());

			try {
				writer.write("mock content here!");

				// no status set by purpose
			} finally {
				t.complete();
			}
		}
	}
}
