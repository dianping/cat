package com.dianping.cat.home.abtest.conditions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TrafficFilterTest {
	private Invocable m_inv;

	public void check(String url, boolean expected) throws ScriptException, NoSuchMethodException, FileNotFoundException {
		HttpServletRequest request = new MockHttpServletRequest(url);

		boolean result = false;
		try{
			result = (Boolean) m_inv.invokeFunction("isEligible", request);
		}catch(Exception e){
			System.out.println(e);
		}
		Assert.assertEquals(expected, result);
	}

	@Before
	public void setup() throws FileNotFoundException, ScriptException {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByExtension("java");
		Reader reader = new FileReader("src/test/resources/com/dianping/cat/home/abtest/conditions/TrafficFilter");
		m_inv = (Invocable) engine.eval(reader);
	}

	@Test
	public void whether_following_urls_can_be_filtered() throws FileNotFoundException, ScriptException,
	      NoSuchMethodException {
		check("http://www.dianping.com", true);
		check("http://www.dianping.com1", false);
		check("http://www.dianping.com/1", false);
		check("http://www.dianping.com/2", false);
	}

	@SuppressWarnings("rawtypes")
	class MockHttpServletRequest implements HttpServletRequest {

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
