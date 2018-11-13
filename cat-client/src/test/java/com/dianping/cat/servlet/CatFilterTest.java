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
package com.dianping.cat.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
		context.addFilter(CatFilter.class, "/*", Handler.REQUEST);
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
								.header("X-CAT-TRACE-MODE", "true") //
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

		TimeUnit.MILLISECONDS.sleep(100);
	}

	private String getHeader(Map<String, List<String>> headers, String name) {
		List<String> values = headers.get(name);

		if (values != null) {
			int len = values.size();

			if (len == 0) {
				return null;
			} else if (len == 1) {
				return values.get(0);
			} else {
				return Joiners.by(',').join(values);
			}
		} else {
			return null;
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
