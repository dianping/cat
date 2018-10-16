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

import java.util.LinkedHashMap;
import java.util.Map;

public class ServerGroupByEntity {

	private String m_measurement;

	private String m_endPoint;

	private Map<Long, Double> m_values = new LinkedHashMap<Long, Double>();

	public ServerGroupByEntity(String measurement, String endPoint, Map<Long, Double> values) {
		m_measurement = measurement;
		m_endPoint = endPoint;
		m_values = values;
	}

	public String getEndPoint() {
		return m_endPoint;
	}

	public String getMeasurement() {
		return m_measurement;
	}

	public Map<Long, Double> getValues() {
		return m_values;
	}

}
