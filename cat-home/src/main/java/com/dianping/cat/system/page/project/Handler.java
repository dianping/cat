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
package com.dianping.cat.system.page.project;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.SystemPage;

public class Handler implements PageHandler<Context> {
	@Inject
	public ProjectService m_projectService;

	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "project")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "project")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case DOMAINS:
			JsonBuilder jb = new JsonBuilder();
			Set<String> domains = m_projectService.findAllDomains();
			Map<String, Object> jsons = new HashMap<String, Object>();

			jsons.put("domains", domains);
			model.setContent(jb.toJson(jsons));
			break;
		case PROJECT_UPDATE:
			try {
				Project project = payload.getProject();

				if (project.getDomain() == null) {
					project.setDomain(Constants.CAT);
				}

				Project temp = m_projectService.findByDomain(project.getDomain());

				if (temp == null) {
					m_projectService.insert(project);
				} else {
					m_projectService.update(project);
				}
				model.setContent(UpdateStatus.SUCCESS.getStatusJson());
			} catch (Exception e) {
				model.setContent(UpdateStatus.INTERNAL_ERROR.getStatusJson());
				Cat.logError(e);
			}
			break;
		}

		model.setAction(action);
		model.setPage(SystemPage.PROJECT);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
