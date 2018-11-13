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
package com.dianping.cat.mvc;

import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class ApiPayload {

	@FieldMeta("ip")
	private String m_ipAddress;

	@FieldMeta("messageId")
	private String m_messageId;

	@FieldMeta("waterfall")
	private boolean m_waterfall;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("city")
	private String m_city;

	@FieldMeta("channel")
	private String m_channel;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("metricType")
	private String m_metricType;

	@FieldMeta("database")
	private String m_database;

	@FieldMeta("province")
	private String m_province;

	@FieldMeta("queryType")
	private String m_queryType;

	@FieldMeta("min")
	private int m_min = -1;

	@FieldMeta("max")
	private int m_max = -1;

	@FieldMeta("cdn")
	private String m_cdn = "ALL";

	public String getCdn() {
		return m_cdn;
	}

	public void setCdn(String cdn) {
		m_cdn = cdn;
	}

	public String getChannel() {
		return m_channel;
	}

	public void setChannel(String channel) {
		m_channel = channel;
	}

	public String getCity() {
		return m_city;
	}

	public void setCity(String city) {
		m_city = city;
	}

	public String getDatabase() {
		return m_database;
	}

	public void setDatabase(String database) {
		m_database = database;
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public int getMax() {
		return m_max;
	}

	public void setMax(int max) {
		m_max = max;
	}

	public String getMessageId() {
		return m_messageId;
	}

	public void setMessageId(String messageId) {
		m_messageId = messageId;
	}

	public String getMetricType() {
		return m_metricType;
	}

	public int getMin() {
		return m_min;
	}

	public void setMin(int min) {
		m_min = min;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getProvince() {
		return m_province;
	}

	public void setProvince(String province) {
		m_province = province;
	}

	public String getQueryType() {
		return m_queryType;
	}

	public void setQueryType(String queryType) {
		m_queryType = queryType;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public boolean isWaterfall() {
		return m_waterfall;
	}

	public void setWaterfall(boolean waterfall) {
		m_waterfall = waterfall;
	}

	public void setMeticType(String metricType) {
		m_metricType = metricType;
	}

}
