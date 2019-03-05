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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.metric.Range;

@ModelMeta("business")
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	private Set<String> m_domains;

	private Set<String> m_tags;

	private Date m_startTime;

	private Date m_endTime;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	public Range[] getAllRange() {
		return Range.values();
	}

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public Set<String> getDomains() {
		return m_domains;
	}

	public void setDomains(Set<String> domains) {
		m_domains = domains;
	}

	public Set<String> getTags() {
		return m_tags;
	}

	public void setTags(Set<String> tags) {
		m_tags = tags;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

}
