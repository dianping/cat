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

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.dianping.cat.Cat;
import com.dianping.cat.CatClientConstants;

public class CatListener implements ServletContextListener {
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Cat.destroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		String catClientXml = ctx.getInitParameter("cat-client-xml");
		File clientXmlFile;

		if (catClientXml != null) {
			clientXmlFile = new File(catClientXml);
		} else {
			clientXmlFile = new File(Cat.getCatHome(), CatClientConstants.CLIENT_XML);
		}

		Cat.getBootstrap().initialize(clientXmlFile);
	}
}
