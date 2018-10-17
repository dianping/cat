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
package com.dianping.cat.report.page.problem.transform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.site.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.GraphTrend;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.problem.Model;
import com.dianping.cat.report.page.problem.Payload;

public class ProblemTrendGraphBuilder {
	private int m_duration = 1;

	private LineChart buildLineChart(Date start, Date end, long step, int size) {
		LineChart item = new LineChart();

		item.setStart(start);
		item.setSize(size);
		item.setStep(step);
		item.setSubTitles(buildSubTitles(start, end));
		return item;
	}

	private String buildSubTitle(Date start, Date end) {
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat to = new SimpleDateFormat("MM-dd");
		StringBuilder sb = new StringBuilder();

		sb.append(from.format(start)).append("~").append(to.format(end));
		return sb.toString();
	}

	private List<String> buildSubTitles(Date start, Date end) {
		List<String> subTitles = new ArrayList<String>();

		subTitles.add(buildSubTitle(start, end));
		return subTitles;
	}

	public void buildTrendGraph(Model model, Payload payload, ProblemReport report) {
		String name = payload.getStatus();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String reportType = payload.getReportType();
		String ip = payload.getIpAddress();
		String type = payload.getType();

		double[] data = getDatas(report, ip, type, name);

		ReportType queryType = ReportType.findByName(reportType);
		long step = queryType.getStep() * m_duration;
		int size = (int) ((start.getTime() - end.getTime()) / step);

		LineChart fail = buildLineChart(start, end, step, size);

		fail.setTitle(queryType.getFailTitle());
		fail.addValue(data);

		model.setErrorsTrend(fail.getJsonString());
	}

	private double[] getDatas(ProblemReport report, String ip, String type, String status) {
		ProblemReportVisitor visitor = new ProblemReportVisitor(ip, type, status);
		visitor.visitProblemReport(report);

		return visitor.getDatas();
	}

	public enum ReportType {

		DAY("day", TimeHelper.ONE_MINUTE) {
			@Override
			String getFailTitle() {
				return "错误量 (count/min)";
			}
		},

		WEEK("week", TimeHelper.ONE_DAY) {
			@Override
			String getFailTitle() {
				return "错误量 (count/day)";
			}
		},

		MONTH("month", TimeHelper.ONE_DAY) {
			@Override
			String getFailTitle() {
				return "错误量 (count/day)";
			}

		};

		private String m_name;

		private long m_step;

		private ReportType(String name, long step) {
			m_name = name;
			m_step = step;
		}

		public static ReportType findByName(String name) {
			for (ReportType type : ReportType.values()) {
				if (type.getName().equalsIgnoreCase(name)) {
					return type;
				}
			}
			throw new RuntimeException("Error graph query type");
		}

		abstract String getFailTitle();

		public String getName() {
			return m_name;
		}

		public long getStep() {
			return m_step;
		}
	}

	public class ProblemReportVisitor extends BaseVisitor {

		double[] m_fails;

		private String m_ip;

		private String m_type;

		private String m_status;

		public ProblemReportVisitor(String ip, String type, String status) {
			m_ip = ip;
			m_type = type;
			m_status = status;
		}

		public double[] getDatas() {
			return m_fails;
		}

		private double[] parseToDouble(String str) {
			if (StringUtils.isNotEmpty(str)) {
				String[] strs = str.split(GraphTrendUtil.GRAPH_SPLITTER);
				double[] result = new double[strs.length];

				for (int i = 0; i < strs.length; i++) {
					try {
						result[i] = Double.parseDouble(strs[i]);
					} catch (Exception e) {
						result[i] = 0.0;
						Cat.logError(e);
					}
				}
				return result;
			} else {
				return null;
			}
		}

		private void resolveGraphTrend(GraphTrend graph) {
			if (graph != null) {
				m_duration = graph.getDuration();
				double[] tmp = parseToDouble(graph.getFails());

				if (m_fails == null) {
					m_fails = new double[tmp.length];
				}

				for (int i = 0; i < m_fails.length; i++) {
					m_fails[i] += tmp[i];
				}
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			if (Constants.ALL.equalsIgnoreCase(m_ip)) {
				super.visitMachine(machine);
			} else {
				if (machine.getIp().equalsIgnoreCase(m_ip)) {
					super.visitMachine(machine);
				}
			}
		}

		@Override
		public void visitEntity(Entity entity) {
			if (StringUtils.isEmpty(m_status)) {
				if (entity.getType().equalsIgnoreCase(m_type)) {
					GraphTrend graphTrend = entity.getGraphTrend();
					resolveGraphTrend(graphTrend);
				}
			} else {
				if (entity.getType().equalsIgnoreCase(m_type) && entity.getStatus().equalsIgnoreCase(m_status)) {
					GraphTrend graphTrend = entity.getGraphTrend();
					resolveGraphTrend(graphTrend);
				}
			}
		}
	}
}
