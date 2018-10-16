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
package com.dianping.cat.report.page.state;

import java.util.Set;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeHelper;

public class StateHourlyGraphVisitor extends StateDisplay {

	protected Double[] m_data = null;

	protected int m_maxSize;

	protected long m_start;

	protected String m_domain = "";

	protected String m_attribute = "";

	public StateHourlyGraphVisitor(String ip, Set<String> fakeDomains, String key, int maxSize) {
		super(ip, fakeDomains);

		m_maxSize = maxSize;
		int index = key.indexOf(":");

		if (index > -1) {
			m_domain = key.substring(0, index);
			m_attribute = key.substring(index + 1);
		} else {
			m_attribute = key;
		}
	}

	public Double[] getData() {
		return m_data;
	}

	@Override
	public void visitDetail(Detail detail) {
		super.visitDetail(detail);

		long id = detail.getId();
		Detail d = m_processDomain.findOrCreateDetail(detail.getId());
		int minute = (int) ((id - m_start) / TimeHelper.ONE_MINUTE);

		if (m_attribute.equalsIgnoreCase("total")) {
			m_data[minute] = (double) d.getTotal();
		} else if (m_attribute.equalsIgnoreCase("totalLoss")) {
			m_data[minute] = (double) d.getTotalLoss();
		} else if (m_attribute.equalsIgnoreCase("size")) {
			m_data[minute] = (double) d.getSize() / 1024 / 1024;
		}
	}

	@Override
	public void visitMessage(Message message) {
		if (StringUtils.isEmpty(m_domain)) {
			super.visitMessage(message);

			long id = message.getId();
			Message msg = getMachine().findOrCreateMessage(id);
			int minute = (int) (id - m_start) / 60000;

			if (m_attribute.equalsIgnoreCase("total")) {
				m_data[minute] = (double) msg.getTotal();
			} else if (m_attribute.equalsIgnoreCase("totalLoss")) {
				m_data[minute] = (double) msg.getTotalLoss();
			} else if (m_attribute.equalsIgnoreCase("avgTps")) {
				m_data[minute] = (double) msg.getTotal();
			} else if (m_attribute.equalsIgnoreCase("maxTps")) {
				m_data[minute] = (double) msg.getTotal();
			} else if (m_attribute.equalsIgnoreCase("dump")) {
				m_data[minute] = (double) msg.getDump();
			} else if (m_attribute.equalsIgnoreCase("dumpLoss")) {
				m_data[minute] = (double) msg.getDumpLoss();
			} else if (m_attribute.equalsIgnoreCase("pigeonTimeError")) {
				m_data[minute] = (double) msg.getPigeonTimeError();
			} else if (m_attribute.equalsIgnoreCase("networkTimeError")) {
				m_data[minute] = (double) msg.getNetworkTimeError();
			} else if (m_attribute.equalsIgnoreCase("blockTotal")) {
				m_data[minute] = (double) msg.getBlockTotal();
			} else if (m_attribute.equalsIgnoreCase("blockLoss")) {
				m_data[minute] = (double) msg.getBlockLoss();
			} else if (m_attribute.equalsIgnoreCase("blockTime")) {
				m_data[minute] = (double) msg.getBlockTime() * 1.0 / 60 / 1000;
			} else if (m_attribute.equalsIgnoreCase("size")) {
				m_data[minute] = (double) msg.getSize() / 1024 / 1024;
			} else if (m_attribute.equalsIgnoreCase("delayAvg")) {
				if (msg.getDelayCount() > 0) {
					m_data[minute] = msg.getDelaySum() / msg.getDelayCount();
				}
			}
		}
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		if (StringUtils.isNotEmpty(m_domain) && m_domain.equals(processDomain.getName())) {
			super.visitProcessDomain(processDomain);
		}
	}

	@Override
	public void visitStateReport(StateReport stateReport) {
		m_data = new Double[m_maxSize];
		m_start = stateReport.getStartTime().getTime();
		long current = System.currentTimeMillis();
		current -= current % Constants.HOUR;
		long size = m_maxSize;

		if (m_start == current) {
			long minute = (System.currentTimeMillis()) / 1000 / 60 % 60;
			size = (int) minute + 1;
		}
		for (int i = 0; i < size; i++) {
			m_data[i] = 0.0;
		}
		super.visitStateReport(stateReport);
	}

}
