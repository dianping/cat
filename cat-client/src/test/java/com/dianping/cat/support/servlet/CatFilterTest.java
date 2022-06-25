/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.support.servlet;

import static com.dianping.cat.Cat.PropertyContext.CHILD_ID;
import static com.dianping.cat.Cat.PropertyContext.PARENT_ID;
import static com.dianping.cat.Cat.PropertyContext.ROOT_ID;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.PropertyConsumer;
import com.dianping.cat.Cat.PropertyContext;
import com.dianping.cat.CatClientConstants;
import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.MessageAssert;
import com.dianping.cat.message.MessageAssert.HeaderAssert;
import com.dianping.cat.message.MessageAssert.TransactionAssert;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.support.Files;
import com.dianping.cat.support.Urls;
import com.dianping.cat.support.Urls.UrlIO;
import com.github.netty.StartupServer;
import com.github.netty.protocol.HttpServletProtocol;
import com.github.netty.protocol.servlet.ServletContext;

public class CatFilterTest extends ComponentTestCase {
	private HttpServer m_server;

	@After
	public void after() {
		m_server.close();
		MessageAssert.reset();
	}

	@Before
	public void before() {
		MessageAssert.intercept(Cat.getBootstrap().getComponentContext());

		Cat.getBootstrap().initializeByDomain("mockApp");

		m_server = new HttpServer();
		m_server.start();
	}

	private String httpCall(String uri, PropertyContext ctx) throws IOException {
		String url = "http://localhost:2282" + uri;
		final UrlIO u = Urls.forIO().connectTimeout(1000);
		Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();

		if (ctx != null) {
			ctx.forEach(new PropertyConsumer() {
				@Override
				public void accept(String name, String value) {
					if (name.equals(CHILD_ID) || name.equals(PARENT_ID) || name.equals(ROOT_ID)) {
						u.header(name, value);
					}
				}
			});
		}

		InputStream in = u.openStream(url, responseHeaders);
		String content = Files.forIO().readFrom(in, "utf-8");

		return content;
	}

	@Test
	public void testMode0() throws Exception {
		Assert.assertEquals("/mock/mode0", httpCall("/mock/mode0?k1=v1&k2=v2", null));

		TransactionAssert ta = MessageAssert.transaction().type("URL").name("/mock/mode0").success().complete();

		ta.childEvent(0).type("URL").name("URL.Server");
		ta.childEvent(1).type("URL").name("URL.Method").data("HTTP/GET /mock/mode0?k1=v1&k2=v2");
		ta.childTransaction(0).type("MockServlet").name("/mode0");
	}

	@Test
	public void testMode1() throws Exception {
		Transaction t = Cat.newTransaction("CatFilterTest", "testMode1");
		PropertyContext ctx = new PropertyContext("/mock/mode1");

		Cat.logRemoteCallClient(ctx);

		Assert.assertEquals("/mock/mode1", httpCall("/mock/mode1?k1=v1&k2=v2", ctx));

		t.success().complete();

		// child message tree
		{
			HeaderAssert ha = MessageAssert.headerByTransaction("URL");

			ha.withMessageId().withParentMessageId().withRootMessageId();

			TransactionAssert ta = MessageAssert.transactionBy("URL").name("/mock/mode1").success().complete();

			ta.childEvent(0).type("URL").name("URL.Server");
			ta.childEvent(1).type("URL").name("URL.Method").data("HTTP/GET /mock/mode1?k1=v1&k2=v2");
			ta.childTransaction(0).type("MockServlet").name("/mode1");
		}

		// parent message tree
		{
			HeaderAssert ha = MessageAssert.headerByTransaction("CatFilterTest");

			ha.withMessageId();

			TransactionAssert ta = MessageAssert.transactionBy("CatFilterTest").name("testMode1").success().complete();

			ta.childEvent(0).type(CatClientConstants.TYPE_REMOTE_CALL).name("/mock/mode1").withData();
		}
	}

	private class HttpServer extends StartupServer {
		public HttpServer() {
			super(2282);

			ServletContext servletContext = new ServletContext();
			EnumSet<DispatcherType> types = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD);

			servletContext.addFilter("cat-filter", new CatFilter()) //
			      .addMappingForUrlPatterns(types, false, "/*");
			servletContext.addServlet("mock-servlet", new MockServlet()) //
			      .addMapping("/mock/*");

			HttpServletProtocol protocol = new HttpServletProtocol(servletContext);
			protocol.setMaxBufferBytes(1024 * 1024);

			getProtocolHandlers().add(protocol);
			getServerListeners().add(protocol);
		}
	}

	private static class MockServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

			Transaction t = Cat.newTransaction("MockServlet", req.getPathInfo());
			PrintWriter writer = res.getWriter();

			Cat.logRemoteCallServer(new PropertyContext(req));

			try {
				writer.write(req.getRequestURI());
			} finally {
				t.success().complete();
			}
		}
	}
}
