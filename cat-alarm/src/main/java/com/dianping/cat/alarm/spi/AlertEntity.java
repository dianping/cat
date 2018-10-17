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
package com.dianping.cat.alarm.spi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlertEntity {

	private Date m_date;

	private String m_type;

	private String m_group;

	private AlertLevel m_level;

	private String m_metric;

	private String m_content;

	private String m_domain;

	private String m_contactGroup;

	private Map<String, Object> m_paras = new HashMap<String, Object>();

	public AlertEntity addPara(String key, Object value) {
		m_paras.put(key, value);
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		AlertEntity other = (AlertEntity) obj;

		if (m_group.equals(other.getGroup()) && m_metric.equals(other.getMetric())) {
			return true;
		} else {
			return false;
		}
	}

	public String getContactGroup() {
		if (m_contactGroup != null) {
			return m_contactGroup;
		} else {
			return m_group;
		}
	}

	public void setContactGroup(String contactGroup) {
		m_contactGroup = contactGroup;
	}

	public String getContent() {
		return m_content;
	}

	public AlertEntity setContent(String content) {
		m_content = content;
		return this;
	}

	public Date getDate() {
		return m_date;
	}

	public AlertEntity setDate(Date alertDate) {
		m_date = alertDate;
		return this;
	}

	public String getDomain() {
		if (m_domain != null) {
			return m_domain;
		} else {
			return m_group;
		}
	}

	public AlertEntity setDomain(String domain) {
		m_domain = domain;
		return this;
	}

	public String getGroup() {
		return m_group;
	}

	public AlertEntity setGroup(String group) {
		m_group = group;
		return this;
	}

	public String getKey() {
		return m_level + ":" + m_type + ":" + m_group + ":" + m_metric;
	}

	public AlertLevel getLevel() {
		return m_level;
	}

	public AlertEntity setLevel(String level) {
		m_level = AlertLevel.findByName(level);
		return this;
	}

	public String getMetric() {
		return m_metric;
	}

	public AlertEntity setMetric(String metricName) {
		m_metric = metricName;
		return this;
	}

	public Object getPara(String key) {
		return m_paras.get(key);
	}

	public Map<String, Object> getParas() {
		return m_paras;
	}

	public AlertEntity setParas(Map<String, Object> paras) {
		m_paras = paras;
		return this;
	}

	public AlertType getType() {
		return AlertType.getTypeByName(m_type);
	}

	public AlertEntity setType(String type) {
		m_type = type;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_group == null) ? 0 : m_group.hashCode());
		result = prime * result + ((m_metric == null) ? 0 : m_metric.hashCode());
		return result;
	}

	public AlertEntity setLevel(AlertLevel level) {
		m_level = level;
		return this;
	}

	@Override
	public String toString() {
		return "AlertEntity [m_date=" + m_date + ", m_type=" + m_type + ", m_group=" + m_group + ", m_level=" + m_level
								+ ", m_metric=" + m_metric + "]";
	}

}
