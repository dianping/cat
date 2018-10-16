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
package com.dianping.cat.report;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.Page;
import org.unidal.webres.resource.runtime.ResourceConfigurator;
import org.unidal.webres.resource.runtime.ResourceInitializer;
import org.unidal.webres.resource.runtime.ResourceRuntime;
import org.unidal.webres.resource.runtime.ResourceRuntimeContext;
import org.unidal.webres.resource.spi.IResourceRegistry;
import org.unidal.webres.tag.resource.ResourceTagConfigurator;
import org.unidal.webres.taglib.basic.ResourceTagLibConfigurator;

public class ReportContext<T extends ActionPayload<? extends Page, ? extends Action>> extends ActionContext<T> {

	@Override
	public void initialize(HttpServletRequest request, HttpServletResponse response) {
		super.initialize(request, response);

		String contextPath = request.getContextPath();

		synchronized (ResourceRuntime.INSTANCE) {
			if (!ResourceRuntime.INSTANCE.hasConfig(contextPath)) {
				ServletContext servletContext = request.getSession().getServletContext();
				File warRoot = new File(servletContext.getRealPath("/"));

				System.out.println("[INFO] Working directory is " + System.getProperty("user.dir"));
				System.out.println("[INFO] War root is " + warRoot);

				ResourceRuntime.INSTANCE.removeConfig(contextPath);
				ResourceInitializer.initialize(contextPath, warRoot);

				IResourceRegistry registry = ResourceRuntime.INSTANCE.getConfig(contextPath).getRegistry();

				new ResourceConfigurator().configure(registry);
				new ResourceTagConfigurator().configure(registry);
				new ResourceTagLibConfigurator().configure(registry);

				registry.lock();
			}

			ResourceRuntimeContext.setup(contextPath);
		}
	}

}
