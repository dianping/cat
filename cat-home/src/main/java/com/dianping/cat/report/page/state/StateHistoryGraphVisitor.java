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

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;

public class StateHistoryGraphVisitor extends BaseVisitor {

	private double[] m_data = null;

	private String m_ip;

	private long m_start;

	private long m_currentStart;

	private String m_attribute = "";

	private long m_gap;

	private StateReport m_stateReport;

	public StateHistoryGraphVisitor(String ip, long start, long end, String key) {
		m_ip = ip;
		m_start = start;
		m_attribute = key;

		if (end - start > TimeHelper.ONE_DAY) {
			m_gap = TimeHelper.ONE_DAY;
		} else {
			m_gap = TimeHelper.ONE_HOUR;
		}
		int size = (int) ((end - start) / m_gap);
		m_data = new double[size];
	}

	public double[] getData() {
		return m_data;
	}

	protected Machine mergerMachine(Machine old, Machine other) {
		old.setAvgTps(old.getAvgTps() + other.getAvgTps());
		old.setTotal(old.getTotal() + other.getTotal());
		old.setTotalLoss(old.getTotalLoss() + other.getTotalLoss());
		old.setDump(old.getDump() + other.getDump());
		old.setDumpLoss(old.getDumpLoss() + other.getDumpLoss());
		old.setSize(old.getSize() + other.getSize());
		old.setDelaySum(old.getDelaySum() + other.getDelaySum());
		old.setDelayCount(old.getDelayCount() + other.getDelayCount());
		old.setBlockTotal(old.getBlockTotal() + other.getBlockTotal());
		old.setBlockLoss(old.getBlockLoss() + other.getBlockLoss());
		old.setBlockTime(old.getBlockTime() + other.getBlockTime());
		old.setPigeonTimeError(old.getPigeonTimeError() + other.getPigeonTimeError());
		old.setNetworkTimeError(old.getNetworkTimeError() + other.getNetworkTimeError());

		if (other.getMaxTps() > old.getMaxTps()) {
			old.setMaxTps(other.getMaxTps());
		}

		long count = old.getDelayCount();
		double sum = old.getDelaySum();

		if (count > 0) {
			old.setDelayAvg(sum / count);
		}
		return old;
	}

	@Override
	public void visitDetail(Detail detail) {
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();
		Machine m = m_stateReport.findOrCreateMachine(m_ip);

		if (m_ip.equals(Constants.ALL) || m_ip.equalsIgnoreCase(ip)) {
			if (m_gap == TimeHelper.ONE_HOUR) {
				for (Message message : machine.getMessages().values()) {
					visitMessage(message);
				}
			} else {
				mergerMachine(m, machine);
				int day = (int) ((m_currentStart - m_start) / TimeHelper.ONE_DAY);

				if (m_attribute.equalsIgnoreCase("total")) {
					m_data[day] = m.getTotal();
				} else if (m_attribute.equalsIgnoreCase("totalLoss")) {
					m_data[day] = m.getTotalLoss();
				} else if (m_attribute.equalsIgnoreCase("avgTps")) {
					m_data[day] = m.getAvgTps();
				} else if (m_attribute.equalsIgnoreCase("maxTps")) {
					m_data[day] = m.getMaxTps();
				} else if (m_attribute.equalsIgnoreCase("dump")) {
					m_data[day] = m.getDump();
				} else if (m_attribute.equalsIgnoreCase("dumpLoss")) {
					m_data[day] = m.getDumpLoss();
				} else if (m_attribute.equalsIgnoreCase("pigeonTimeError")) {
					m_data[day] = m.getPigeonTimeError();
				} else if (m_attribute.equalsIgnoreCase("networkTimeError")) {
					m_data[day] = m.getNetworkTimeError();
				} else if (m_attribute.equalsIgnoreCase("blockTotal")) {
					m_data[day] = m.getBlockTotal();
				} else if (m_attribute.equalsIgnoreCase("blockLoss")) {
					m_data[day] = m.getBlockLoss();
				} else if (m_attribute.equalsIgnoreCase("blockTime")) {
					m_data[day] = m.getBlockTime() * 1.0 / 60 / 1000;
				} else if (m_attribute.equalsIgnoreCase("size")) {
					m_data[day] = m.getSize() / 1024 / 1024;
				} else if (m_attribute.equalsIgnoreCase("delayAvg")) {
					if (m.getDelayCount() > 0) {
						m_data[day] = m.getDelaySum() / m.getDelayCount();
					}
				}
			}
		}
	}

	@Override
	public void visitMessage(Message message) {
		int hour = (int) ((m_currentStart - m_start) / TimeHelper.ONE_HOUR);

		if (m_attribute.equalsIgnoreCase("total")) {
			m_data[hour] += (double) message.getTotal();
		} else if (m_attribute.equalsIgnoreCase("totalLoss")) {
			m_data[hour] += (double) message.getTotalLoss();
		} else if (m_attribute.equalsIgnoreCase("avgTps")) {
			m_data[hour] += (double) message.getTotal();
		} else if (m_attribute.equalsIgnoreCase("maxTps")) {
			m_data[hour] += (double) message.getTotal();
		} else if (m_attribute.equalsIgnoreCase("dump")) {
			m_data[hour] += (double) message.getDump();
		} else if (m_attribute.equalsIgnoreCase("dumpLoss")) {
			m_data[hour] += (double) message.getDumpLoss();
		} else if (m_attribute.equalsIgnoreCase("pigeonTimeError")) {
			m_data[hour] += (double) message.getPigeonTimeError();
		} else if (m_attribute.equalsIgnoreCase("networkTimeError")) {
			m_data[hour] += (double) message.getNetworkTimeError();
		} else if (m_attribute.equalsIgnoreCase("blockTotal")) {
			m_data[hour] += (double) message.getBlockTotal();
		} else if (m_attribute.equalsIgnoreCase("blockLoss")) {
			m_data[hour] += (double) message.getBlockLoss();
		} else if (m_attribute.equalsIgnoreCase("blockTime")) {
			m_data[hour] += (double) message.getBlockTime() * 1.0 / 60 / 1000;
		} else if (m_attribute.equalsIgnoreCase("size")) {
			m_data[hour] += (double) message.getSize() / 1024 / 1024;
		} else if (m_attribute.equalsIgnoreCase("delayAvg")) {
			if (message.getDelayCount() > 0) {
				m_data[hour] += message.getDelaySum() / message.getDelayCount();
			}
		}
	}

	@Override
	public void visitStateReport(StateReport stateReport) {
		m_currentStart = stateReport.getStartTime().getTime();
		m_stateReport = new StateReport().setDomain(stateReport.getDomain()).setStartTime(stateReport.getStartTime())
								.setEndTime(stateReport.getEndTime());

		super.visitStateReport(stateReport);
	}

}
