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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.web.AbstractContainerServlet;

import com.dianping.cat.Cat;

public class CatServlet extends AbstractContainerServlet {
	private static final long serialVersionUID = 1L;

	private Exception m_exception;

	private File getConfigFile(ServletConfig config, String name, String defaultConfigValue) {
		String configValue = config.getInitParameter(name);

		if (configValue != null) {
			if (configValue.startsWith("/")) {
				return new File(configValue);
			} else {
				return new File(Cat.getCatHome(), configValue);
			}
		} else {
			return new File(Cat.getCatHome(), defaultConfigValue);
		}
	}

	@Override
	protected void initComponents(ServletConfig servletConfig) throws ServletException {
		try {
			ModuleContext ctx = new DefaultModuleContext(getContainer());
			ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);
			File clientXmlFile = getConfigFile(servletConfig, "cat-client-xml", "client.xml");
			File serverXmlFile = getConfigFile(servletConfig, "cat-server-xml", "server.xml");

			ctx.setAttribute("cat-client-config-file", clientXmlFile);
			ctx.setAttribute("cat-server-config-file", serverXmlFile);
			initializer.execute(ctx);
		} catch (Exception e) {
			m_exception = e;
			System.err.println(e);
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setCharacterEncoding("utf-8");
		res.setContentType("text/plain");

		PrintWriter writer = res.getWriter();

		if (m_exception != null) {
			writer.write("Server has NOT been initialized successfully!\r\n\r\n");
			m_exception.printStackTrace(writer);
		} else {
			writer.write("Not implemented yet!");
		}
	}
}