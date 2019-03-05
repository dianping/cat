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
package com.dianping.cat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.servlet.GzipFilter;
import org.unidal.test.jetty.JettyServer;

@RunWith(JUnit4.class)
public class TestServer extends JettyServer {
	public static void main(String[] args) throws Exception {
		TestServer server = new TestServer();
		System.setProperty("devMode", "true");
		server.startServer();
		server.startWebApp();
		server.stopServer();
	}

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
	}

	@Override
	protected String getContextPath() {
		return "/cat";
	}

	@Override
	protected int getServerPort() {
		return 8080;
	}

	@Override
	protected void postConfigure(WebAppContext context) {
		context.addFilter(GzipFilter.class, "/*", Handler.ALL);
	}

	@Test
	public void startWebApp() throws Exception {
		super.startServer();
		display("/cat/r");
		waitForAnyKey();
	}

}
