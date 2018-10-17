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

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Constants;

public class MetricEntity {

	private String m_category;

	private String m_measure;

	private long m_timestamp;

	private Map<String, String> m_tags = new HashMap<String, String>();

	private Map<String, Object> m_fields = new HashMap<String, Object>();

	public MetricEntity(String category, String measure, String endPoint, long timestamp) {
		m_category = category;
		m_measure = measure;
		m_timestamp = timestamp;

		m_tags.put(Constants.END_POINT, endPoint);
	}

	public void addField(String field, Object value) {
		m_fields.put(field, value);
	}

	public void addFields(Map<String, Object> fields) {
		m_fields.putAll(fields);
	}

	public void addTag(String tag, String value) {
		m_tags.put(tag, value);
	}

	public void addTags(Map<String, String> tags) {
		m_tags.putAll(tags);
	}

	public String getCategory() {
		return m_category;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public String getEndPoint() {
		return m_tags.get(Constants.END_POINT);
	}

	public Map<String, Object> getFields() {
		return m_fields;
	}

	public void setFields(Map<String, Object> fields) {
		m_fields = fields;
	}

	public String getMeasure() {
		return m_measure;
	}

	public void setMeasure(String measure) {
		m_measure = measure;
	}

	public Map<String, String> getTags() {
		return m_tags;
	}

	public void setTags(Map<String, String> tags) {
		m_tags = tags;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "MetricEntity [m_category=" + m_category + ", m_measure=" + m_measure + ", m_timestamp=" + m_timestamp
								+ ", m_tags=" + m_tags + ", m_fields=" + m_fields + "]";
	}
}
