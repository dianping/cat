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
package com.dianping.cat.report.page.transaction.transform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.site.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.GraphTrendUtil;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.transaction.Model;
import com.dianping.cat.report.page.transaction.Payload;

public class TransactionTrendGraphBuilder {

	public static final String COUNT = "count";

	public static final String FAIL = "fail";

	public static final String AVG = "avg";

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

	public void buildTrendGraph(Model model, Payload payload, TransactionReport report) {
		String name = payload.getName();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String reportType = payload.getReportType();
		String ip = payload.getIpAddress();
		String type = payload.getType();
		String display = name != null ? name : type;

		Map<String, double[]> data = getDatas(report, ip, type, name);

		ReportType queryType = ReportType.findByName(reportType);
		long step = queryType.getStep() * m_duration;
		int size = (int) ((start.getTime() - end.getTime()) / step);

		LineChart fail = buildLineChart(start, end, step, size);
		LineChart count = buildLineChart(start, end, step, size);
		LineChart avg = buildLineChart(start, end, step, size);

		fail.setTitle(display + queryType.getFailTitle());
		count.setTitle(display + queryType.getSumTitle());
		avg.setTitle(display + queryType.getResponseTimeTitle());

		fail.addValue(data.get(FAIL));
		count.addValue(data.get(COUNT));
		avg.addValue(data.get(AVG));

		model.setErrorTrend(fail.getJsonString());
		model.setHitTrend(count.getJsonString());
		model.setResponseTrend(avg.getJsonString());
	}

	private Map<String, double[]> getDatas(TransactionReport report, String ip, String type, String name) {
		TransactionReportVisitor visitor = new TransactionReportVisitor(ip, type, name);
		visitor.visitTransactionReport(report);

		return visitor.getDatas();
	}

	public enum ReportType {

		DAY("day", TimeHelper.ONE_MINUTE) {
			@Override
			String getFailTitle() {
				return " Error (count/min)";
			}

			@Override
			String getResponseTimeTitle() {
				return " Response Time (ms)";
			}

			@Override
			String getSumTitle() {
				return " Hits (count/min)";
			}

		},

		WEEK("week", TimeHelper.ONE_DAY) {
			@Override
			String getFailTitle() {
				return " Error (count/day)";
			}

			@Override
			String getResponseTimeTitle() {
				return " Response Time (ms)";
			}

			@Override
			String getSumTitle() {
				return " Hits (count/day)";
			}
		},

		MONTH("month", TimeHelper.ONE_DAY) {
			@Override
			String getFailTitle() {
				return " Error (count/day)";
			}

			@Override
			String getResponseTimeTitle() {
				return " Response Time (ms)";
			}

			@Override
			String getSumTitle() {
				return " Hits (count/day)";
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

		abstract String getResponseTimeTitle();

		public long getStep() {
			return m_step;
		}

		abstract String getSumTitle();
	}

	public class TransactionReportVisitor extends BaseVisitor {

		private String m_ip;

		private String m_type;

		private String m_name;

		private Map<String, double[]> m_datas = new HashMap<String, double[]>();

		public TransactionReportVisitor(String ip, String type, String name) {
			m_ip = ip;
			m_type = type;
			m_name = name;
		}

		public Map<String, double[]> getDatas() {
			return m_datas;
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
				m_datas.put(AVG, parseToDouble(graph.getAvg()));
				m_datas.put(COUNT, parseToDouble(graph.getCount()));
				m_datas.put(FAIL, parseToDouble(graph.getFails()));
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			if (machine.getIp().equalsIgnoreCase(m_ip)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(TransactionName name) {
			String id = name.getId();

			if (StringUtils.isNotEmpty(id) && id.equalsIgnoreCase(m_name)) {
				resolveGraphTrend(name.getGraphTrend());
			}
		}

		@Override
		public void visitType(TransactionType type) {
			String id = type.getId();

			if (id.equalsIgnoreCase(m_type)) {
				if (StringUtils.isEmpty(m_name)) {
					resolveGraphTrend(type.getGraphTrend());
				} else {
					super.visitType(type);
				}
			}
		}
	}

}
