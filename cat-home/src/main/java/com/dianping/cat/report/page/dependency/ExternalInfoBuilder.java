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
package com.dianping.cat.report.page.dependency;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class ExternalInfoBuilder {

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_problemservice;

	@Inject
	private DependencyReportService m_reportService;

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	public void buildExceptionInfoOnGraph(Payload payload, Model model, TopologyGraph graph) {
		if (graph.getStatus() != GraphConstrant.OK) {
			String problemInfo = buildProblemInfo(graph.getId(), payload);

			graph.setDes(graph.getDes() + problemInfo);
		}
		for (TopologyNode node : graph.getNodes().values()) {
			node.setLink(buildTopologyNodeLink(payload, model, node.getId()));

			if (node.getType().equals(GraphConstrant.PROJECT)) {
				if (node.getStatus() != GraphConstrant.OK) {
					String problemInfo = buildProblemInfo(node.getId(), payload);

					node.setDes(node.getDes() + problemInfo);
				}
			}
		}
	}

	public void buildNodeExceptionInfo(TopologyNode node, Model model, Payload payload) {
		String domain = node.getId();
		if (node.getStatus() != GraphConstrant.OK) {
			String exceptionInfo = buildProblemInfo(domain, payload);

			node.setDes(node.getDes() + exceptionInfo);
		}
	}

	private String buildProblemInfo(String domain, Payload payload) {
		ProblemReport report = queryProblemReport(payload, domain);
		ProblemInfoVisitor visitor = new ProblemInfoVisitor();

		visitor.visitProblemReport(report);
		return visitor.buildExceptionInfo();
	}

	private String buildTopologyNodeLink(Payload payload, Model model, String domain) {
		return String.format("?op=dependencyGraph&minute=%s&domain=%s&date=%s", model.getMinute(), domain,
								m_dateFormat.format(new Date(payload.getDate())));
	}

	private ProblemReport queryProblemReport(Payload payload, String domain) {
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
								.setProperty("date", date).setProperty("type", "view");
		if (m_problemservice.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_problemservice.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligible problem service registered for " + request + "!");
		}
	}

}