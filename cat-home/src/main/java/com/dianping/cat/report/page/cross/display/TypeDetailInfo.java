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

import com.dianping.cat.consumer.cross.model.entity.Type;

public class TypeDetailInfo {

	private double m_avg;

	private long m_failureCount;

	private double m_failurePercent;

	private String m_projectName;

	private long m_seconds;

	private double m_sum;

	private long m_totalCount;

	private double m_tps;

	private String m_type;

	private String m_ip;

	public TypeDetailInfo(long seconds) {
		m_seconds = seconds;
	}

	public TypeDetailInfo(long seconds, String projectName) {
		m_seconds = seconds;
		m_projectName = projectName;
	}

	public double getAvg() {
		return m_avg;
	}

	public TypeDetailInfo setAvg(double avg) {
		m_avg = avg;
		return this;
	}

	public long getFailureCount() {
		return m_failureCount;
	}

	public TypeDetailInfo setFailureCount(long failureCount) {
		m_failureCount = failureCount;
		return this;
	}

	public double getFailurePercent() {
		return m_failurePercent;
	}

	public TypeDetailInfo setFailurePercent(double failrePercent) {
		m_failurePercent = failrePercent;
		return this;
	}

	public String getIp() {
		return m_ip;
	}

	public TypeDetailInfo setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public String getProjectName() {
		return m_projectName;
	}

	public TypeDetailInfo setProjectName(String projectName) {
		m_projectName = projectName;
		return this;
	}

	public double getSum() {
		return m_sum;
	}

	public TypeDetailInfo setSum(double sum) {
		m_sum = sum;
		return this;
	}

	public long getTotalCount() {
		return m_totalCount;
	}

	public TypeDetailInfo setTotalCount(long totalCount) {
		m_totalCount = totalCount;
		return this;
	}

	public double getTps() {
		return m_tps;
	}

	public String getType() {
		return m_type;
	}

	public TypeDetailInfo setType(String type) {
		m_type = type;
		return this;
	}

	public TypeDetailInfo mergeType(Type type) {
		m_type = type.getId();
		m_totalCount += type.getTotalCount();
		m_failureCount += type.getFailCount();
		m_sum += type.getSum();

		if (m_totalCount > 0) {
			m_avg = m_sum / (double) m_totalCount;
			m_failurePercent = (double) m_failureCount / (double) m_totalCount;
		}
		if (m_seconds > 0) {
			m_tps = m_totalCount / (double) m_seconds;
		}
		return this;
	}

	public TypeDetailInfo mergeTypeDetailInfo(TypeDetailInfo type) {
		m_totalCount += type.getTotalCount();
		m_failureCount += type.getFailureCount();
		m_sum += type.getSum();

		if (m_totalCount > 0) {
			m_avg = m_sum / (double) m_totalCount;
			m_failurePercent = (double) m_failureCount / (double) m_totalCount;
		}
		if (m_seconds > 0) {
			m_tps = m_totalCount / (double) m_seconds;
		}
		return this;
	}

}
