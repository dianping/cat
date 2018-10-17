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
package com.dianping.cat.report.page.cross.display;

import com.dianping.cat.consumer.cross.model.entity.Name;

public class NameDetailInfo {

	private String m_type;

	private double m_avg;

	private long m_failureCount;

	private double m_failurePercent;

	private String m_id;

	private String m_ip;

	private long m_seconds;

	private double m_sum;

	private long m_totalCount;

	private double m_tps;

	public NameDetailInfo(long seconds, String id, String ip, String type) {
		m_seconds = seconds;
		m_id = id;
		m_ip = ip;
		m_type = type;
	}

	public double getAvg() {
		return m_avg;
	}

	public void setAvg(double avg) {
		m_avg = avg;
	}

	public long getFailureCount() {
		return m_failureCount;
	}

	public void setFailureCount(long failureCount) {
		m_failureCount = failureCount;
	}

	public double getFailurePercent() {
		return m_failurePercent;
	}

	public void setFailurePercent(double failrePercent) {
		m_failurePercent = failrePercent;
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		m_id = id;
	}

	public String getIp() {
		return m_ip;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public double getSum() {
		return m_sum;
	}

	public void setSum(double sum) {
		m_sum = sum;
	}

	public long getTotalCount() {
		return m_totalCount;
	}

	public void setTotalCount(long totalCount) {
		m_totalCount = totalCount;
	}

	public double getTps() {
		return m_tps;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void mergeName(Name name) {
		m_totalCount += name.getTotalCount();
		m_failureCount += name.getFailCount();
		m_sum += name.getSum();

		if (m_totalCount > 0) {
			m_avg = m_sum / (double) m_totalCount;
			m_failurePercent = (double) m_failureCount / (double) m_totalCount;
		}
		if (m_seconds > 0) {
			m_tps = m_totalCount / (double) m_seconds;
		}
	}
}
