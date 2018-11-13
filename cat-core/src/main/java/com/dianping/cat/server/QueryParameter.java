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
package com.dianping.cat.server;

import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

public class QueryParameter {

	private String m_category;

	private String m_measurement;

	private String m_tags;

	private MetricType m_type;

	private String m_interval;

	private Date m_start;

	private Date m_end;

	private String m_groupBy;

	private String m_fillValue = "0";

	public String getCategory() {
		return m_category;
	}

	public QueryParameter setCategory(String category) {
		m_category = category;
		return this;
	}

	public Date getEnd() {
		return m_end;
	}

	public QueryParameter setEnd(Date end) {
		m_end = end;
		return this;
	}

	public String getFillValue() {
		return m_fillValue;
	}

	public QueryParameter setFillValue(String fillValue) {
		m_fillValue = fillValue;
		return this;
	}

	public String getGroupBy() {
		return m_groupBy;
	}

	public void setGroupBy(String groupBy) {
		m_groupBy = groupBy;
	}

	public String getInterval() {
		return m_interval;
	}

	public QueryParameter setInterval(String interval) {
		m_interval = interval;
		return this;
	}

	public String getMeasurement() {
		return m_measurement;
	}

	public QueryParameter setMeasurement(String measurement) {
		m_measurement = measurement;
		return this;
	}

	public String getSqlTags() {
		String tag = "";
		List<String> tags = Splitters.by(";").noEmptyItem().split(m_tags);

		if (!tags.isEmpty()) {
			tag = StringUtils.join(tags, " AND ") + " AND ";
		}

		return tag;
	}

	public Date getStart() {
		return m_start;
	}

	public QueryParameter setStart(Date start) {
		m_start = start;
		return this;
	}

	public String getTags() {
		return m_tags;
	}

	public QueryParameter setTags(String tags) {
		m_tags = tags;
		return this;
	}

	public MetricType getType() {
		return m_type;
	}

	public QueryParameter setType(MetricType type) {
		m_type = type;
		return this;
	}
}
