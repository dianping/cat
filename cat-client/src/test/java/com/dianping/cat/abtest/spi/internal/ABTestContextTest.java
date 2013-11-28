package com.dianping.cat.abtest.spi.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.ConversionRule;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.abtest.spi.ABTestContext;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestGroupStrategy;

public class ABTestContextTest extends ComponentTestCase {

	private DefaultABTestContext m_context;

	@Test
	public void prepare() throws Exception {
		Run run = mockRun();
		Case _case = mockCase();
		Invocable invocable = mockInvocable();
		List<ConversionRule> conversionRules = mockConversionRules();

		ABTestEntity entity = new ABTestEntity(_case, run);

		entity.setInvocable(invocable);
		entity.setConversionRules(conversionRules);
		entity.setGroupStrategy(new MockGroupStrategy());

		ABTestContextManager contextManager = lookup(ABTestContextManager.class);

		m_context = (DefaultABTestContext) contextManager.createContext(entity);

		test(1, "100=ab:A");
	}

	private void test(int requestNum, String expectedMetric) {
		for (int i = 0; i < requestNum; i++) {
			MockHttpServletRequest request = new MockHttpServletRequest("http://localhost:8081/cat");
			Map<String, String> cookielets = new HashMap<String, String>();
			m_context.setup(request, null, cookielets);
		}

		Assert.assertEquals(expectedMetric, m_context.getMessageManager().getMetricType());
	}

	private Case mockCase() {
		Case _case = new Case();

		_case.setName("cat");
		_case.setGroupStrategy("cat_groupstrategy");

		return _case;
	}

	private Run mockRun() {
		Run run = new Run();
		run.setId(100);
		run.setDisabled(false);

		return run;
	}

	private Invocable mockInvocable() throws ScriptException {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByExtension("java");
		String javaFragement = "public boolean isEligible(Object object) { return true; }";
		Invocable invocable = (Invocable) engine.eval(javaFragement);
		return invocable;
	}

	private List<ConversionRule> mockConversionRules() {
		List<ConversionRule> conversionRules = new ArrayList<ConversionRule>();
		ConversionRule rule = new ConversionRule();
		rule.setText("http://localhost:8081/not_match");
		ConversionRule rule1 = new ConversionRule();
		rule1.setText("http://localhost:8081/cat");

		conversionRules.add(rule);
		conversionRules.add(rule1);

		return conversionRules;
	}

	public static class MockGroupStrategy implements ABTestGroupStrategy {
		private String a;

		private int b;

		private boolean c;

		private long d;

		private double e;

		private float f;

		@Override
		public void init() {

		}

		@Override
		public void apply(ABTestContext ctx) {
			ctx.setGroupName("A");
		}

		public String getA() {
			return a;
		}

		public int getB() {
			return b;
		}

		public boolean isC() {
			return c;
		}

		public long getD() {
			return d;
		}

		public double getE() {
			return e;
		}

		public float getF() {
			return f;
		}
	}

	@SuppressWarnings("rawtypes")
	public static class MockHttpServletRequest implements HttpServletRequest {

		private String m_url;

		public MockHttpServletRequest(String url) {
			m_url = url;
		}

		@Override
		public Object getAttribute(String name) {
			return null;
		}

		@Override
		public Enumeration getAttributeNames() {
			return null;
		}

		@Override
		public String getAuthType() {
			return null;
		}

		@Override
		public String getCharacterEncoding() {
			return null;
		}

		@Override
		public int getContentLength() {
			return 0;
		}

		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public String getContextPath() {
			return null;
		}

		@Override
		public Cookie[] getCookies() {
			return null;
		}

		@Override
		public long getDateHeader(String name) {
			return 0;
		}

		@Override
		public String getHeader(String name) {
			return null;
		}

		@Override
		public Enumeration getHeaderNames() {
			return null;
		}

		@Override
		public Enumeration getHeaders(String name) {
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public int getIntHeader(String name) {
			return 0;
		}

		@Override
		public String getLocalAddr() {
			return null;
		}

		@Override
		public Locale getLocale() {
			return null;
		}

		@Override
		public Enumeration getLocales() {
			return null;
		}

		@Override
		public String getLocalName() {
			return null;
		}

		@Override
		public int getLocalPort() {
			return 0;
		}

		@Override
		public String getMethod() {
			return null;
		}

		@Override
		public String getParameter(String name) {
			return null;
		}

		@Override
		public Map getParameterMap() {
			return null;
		}

		@Override
		public Enumeration getParameterNames() {
			return null;
		}

		@Override
		public String[] getParameterValues(String name) {
			return null;
		}

		@Override
		public String getPathInfo() {
			return null;
		}

		@Override
		public String getPathTranslated() {
			return null;
		}

		@Override
		public String getProtocol() {
			return null;
		}

		@Override
		public String getQueryString() {
			return null;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return null;
		}

		@Override
		public String getRealPath(String path) {
			return null;
		}

		@Override
		public String getRemoteAddr() {
			return null;
		}

		@Override
		public String getRemoteHost() {
			return null;
		}

		@Override
		public int getRemotePort() {
			return 0;
		}

		@Override
		public String getRemoteUser() {
			return null;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String path) {
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			return null;
		}

		@Override
		public String getRequestURI() {
			return null;
		}

		@Override
		public StringBuffer getRequestURL() {
			StringBuffer sb = new StringBuffer(m_url);
			return sb;
		}

		@Override
		public String getScheme() {
			return null;
		}

		@Override
		public String getServerName() {
			return null;
		}

		@Override
		public int getServerPort() {
			return 0;
		}

		@Override
		public String getServletPath() {
			return null;
		}

		@Override
		public HttpSession getSession() {
			return null;
		}

		@Override
		public HttpSession getSession(boolean create) {
			return null;
		}

		@Override
		public Principal getUserPrincipal() {
			return null;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			return false;
		}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public boolean isUserInRole(String role) {
			return false;
		}

		@Override
		public void removeAttribute(String name) {
		}

		@Override
		public void setAttribute(String name, Object o) {
		}

		@Override
		public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		}
	}

}
