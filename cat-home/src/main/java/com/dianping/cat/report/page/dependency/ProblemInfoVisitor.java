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
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;

public class ProblemInfoVisitor extends BaseVisitor {

	private Map<String, Integer> m_errors = new LinkedHashMap<String, Integer>();

	private Date m_start;

	public String buildExceptionInfo() {
		StringBuilder sb = new StringBuilder();
		Comparator<java.util.Map.Entry<String, Integer>> compator = new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(java.util.Map.Entry<String, Integer> arg0, java.util.Map.Entry<String, Integer> arg1) {
				return arg1.getValue() - arg0.getValue();
			}
		};
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		if (m_errors.size() > 0) {
			sb.append(GraphConstrant.LINE).append(GraphConstrant.ENTER);
			sb.append("<span style='color:red'>").append(Chinese.EXCEPTION_INFO).append("（");
			sb.append(sdf.format(m_start)).append("-").append(sdf.format(new Date(m_start.getTime() + TimeHelper.ONE_HOUR - 1)))
									.append("）");
			sb.append("</span>").append(GraphConstrant.ENTER);
		}
		m_errors = SortHelper.sortMap(m_errors, compator);
		for (java.util.Map.Entry<String, Integer> error : m_errors.entrySet()) {
			sb.append(error.getKey()).append(GraphConstrant.DELIMITER).append(error.getValue()).append(GraphConstrant.ENTER);
		}
		return sb.toString();
	}

	@Override
	public void visitEntity(Entity entity) {
		String type = entity.getType();
		String state = entity.getStatus();

		if ("error".equals(type)) {
			int count = 0;
			for (Duration duration : entity.getDurations().values()) {
				count += duration.getCount();
			}
			Integer temp = m_errors.get(state);

			if (temp == null) {
				m_errors.put(state, count);
			} else {
				m_errors.put(state, temp + count);
			}
		}
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		m_start = problemReport.getStartTime();
		super.visitProblemReport(problemReport);
	}

}
