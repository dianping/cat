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
package com.dianping.cat.report.page.business;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.business.graph.BusinessGraphCreator;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private BusinessGraphCreator m_graphCreator;

	@Inject
	private BusinessTagConfigManager m_tagConfigManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "business")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "business")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);

		Date startDate = payload.getStartDate();
		Date endDate = payload.getEndDate();

		model.setStartTime(startDate);
		model.setEndTime(endDate);

		switch (action) {
		case VIEW:
			Type type = Type.getType(payload.getType(), Type.Domain);
			String name = payload.getName();
			Map<String, LineChart> allCharts = buildLineCharts(type, name, startDate, endDate);

			model.setLineCharts(new ArrayList<LineChart>(allCharts.values()));

			if (type == Type.Domain) {
				model.setDisplayDomain(name);
			}
			break;
		}
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private Map<String, LineChart> buildLineCharts(Type type, String name, Date start, Date end) {
		Map<String, LineChart> allCharts = null;

		if (type == Type.Tag) {
			allCharts = m_graphCreator.buildGraphByTag(start, end, name);
		} else {
			allCharts = m_graphCreator.buildGraphByDomain(start, end, name);
		}

		return allCharts;
	}

	private void normalize(Model model, Payload payload) {
		model.setDomains(m_projectService.findAllDomains());
		model.setTags(m_tagConfigManager.findAllTags());
		model.setPage(ReportPage.BUSINESS);
		model.setAction(payload.getAction());
		m_normalizePayload.normalize(model, payload);
	}
}
