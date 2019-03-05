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
package com.dianping.cat.consumer.state;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.*;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.statistic.ServerStatistic.Statistic;
import com.dianping.cat.statistic.ServerStatisticManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

@Named(type = MessageAnalyzer.class, value = StateAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class StateAnalyzer extends AbstractMessageAnalyzer<StateReport> implements LogEnabled {
	public static final String ID = "state";

	@Inject(ID)
	private ReportManager<StateReport> m_reportManager;

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private ProjectService m_projectService;

	private String m_ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

	private Machine buildStateInfo(Machine machine) {
		long minute = 1000 * 60;
		long start = m_startTime;
		long end = m_startTime + minute * 60;
		double maxTps = 0;
		long current = System.currentTimeMillis();
		int size = 0;

		if (end > current) {
			end = current;
		}
		for (; start < end; start += minute) {
			Statistic state = m_serverStateManager.findOrCreateState(start);
			Message temp = machine.findOrCreateMessage(start);
			Map<String, AtomicLong> totals = state.getMessageTotals();
			Map<String, AtomicLong> totalLosses = state.getMessageTotalLosses();
			Map<String, AtomicLong> sizes = state.getMessageSizes();

			for (Entry<String, AtomicLong> entry : totals.entrySet()) {
				String domain = entry.getKey();
				long value = entry.getValue().get();
				ProcessDomain processDomain = machine.findOrCreateProcessDomain(domain);
				Detail detail = processDomain.findOrCreateDetail(start);

				processDomain.setTotal(value + processDomain.getTotal());
				detail.setTotal(value + detail.getTotal());
			}
			for (Entry<String, AtomicLong> entry : totalLosses.entrySet()) {
				String domain = entry.getKey();
				long value = entry.getValue().get();
				ProcessDomain processDomain = machine.findOrCreateProcessDomain(domain);
				Detail detail = processDomain.findOrCreateDetail(start);

				processDomain.setTotalLoss(value + processDomain.getTotalLoss());
				detail.setTotalLoss(value + detail.getTotalLoss());
			}
			for (Entry<String, AtomicLong> entry : sizes.entrySet()) {
				String domain = entry.getKey();
				long value = entry.getValue().get();
				ProcessDomain processDomain = machine.findOrCreateProcessDomain(domain);
				Detail detail = processDomain.findOrCreateDetail(start);

				processDomain.setSize(value + processDomain.getSize());
				detail.setSize(value + detail.getSize());
			}

			long messageTotal = state.getMessageTotal();
			long messageTotalLoss = state.getMessageTotalLoss();
			long messageSize = state.getMessageSize();
			long blockTotal = state.getBlockTotal();
			long blockLoss = state.getBlockLoss();
			long blockTime = state.getBlockTime();
			long pigeonTimeError = state.getPigeonTimeError();
			long networkTimeError = state.getNetworkTimeError();
			long messageDump = state.getMessageDump();
			long messageDumpLoss = state.getMessageDumpLoss();
			int processDelayCount = state.getProcessDelayCount();
			double processDelaySum = state.getProcessDelaySum();

			temp.setTotal(messageTotal).setTotalLoss(messageTotalLoss).setSize(messageSize);
			temp.setBlockTotal(blockTotal).setBlockLoss(blockLoss).setBlockTime(blockTime);
			temp.setPigeonTimeError(pigeonTimeError).setNetworkTimeError(networkTimeError).setDump(messageDump);
			temp.setDumpLoss(messageDumpLoss).setDelayCount(processDelayCount).setDelaySum(processDelaySum);

			machine.setTotal(messageTotal + machine.getTotal()).setTotalLoss(messageTotalLoss + machine.getTotalLoss())
									.setSize(messageSize + machine.getSize());
			machine.setBlockTotal(machine.getBlockTotal() + blockTotal).setBlockLoss(machine.getBlockLoss() + blockLoss)
									.setBlockTime(machine.getBlockTime() + blockTime);
			machine.setPigeonTimeError(machine.getPigeonTimeError() + pigeonTimeError)
									.setNetworkTimeError(machine.getNetworkTimeError() + networkTimeError)
									.setDump(machine.getDump() + messageDump);
			machine.setDumpLoss(machine.getDumpLoss() + messageDumpLoss)
									.setDelayCount(machine.getDelayCount() + processDelayCount)
									.setDelaySum(machine.getDelaySum() + processDelaySum);

			double avg = 0;
			long count = machine.getDelayCount();

			if (count > 0) {
				avg = machine.getDelaySum() / count;
				machine.setDelayAvg(avg);
			}
			if (messageTotal > maxTps) {
				maxTps = messageTotal;
			}
			temp.setTime(new Date(start));
			size++;
		}

		double avgTps = 0;
		if (size > 0) {
			avgTps = machine.getTotal() / (double) size;
		}
		machine.setAvgTps(avgTps);
		machine.setMaxTps(maxTps);

		return machine;
	}

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();
		StateReport stateReport = getReport(Constants.CAT);
		Map<String, StateReport> reports = m_reportManager.getHourlyReports(startTime);

		reports.put(Constants.CAT, stateReport);
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE, m_index);
		}
		if (atEnd) {
			long minute = 1000 * 60;
			long start = m_startTime - minute * 60 * 2;
			long end = m_startTime - minute * 60;

			for (; start < end; start += minute) {
				m_serverStateManager.removeState(start);
			}
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public StateReport getReport(String domain) {
		StateReport report = new StateReport(Constants.CAT);

		report.setStartTime(new Date(m_startTime));
		report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

		Machine machine = buildStateInfo(report.findOrCreateMachine(m_ip));
		StateReport stateReport = m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, true);
		Map<String, ProcessDomain> processDomains = stateReport.findOrCreateMachine(m_ip).getProcessDomains();

		for (Map.Entry<String, ProcessDomain> entry : machine.getProcessDomains().entrySet()) {
			ProcessDomain processDomain = processDomains.get(entry.getKey());

			if (processDomain != null) {
				entry.getValue().getIps().addAll(processDomain.getIps());
			}
		}
		return report;
	}

	@Override
	public ReportManager<StateReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		List<Heartbeat> heartbeats = tree.getHeartbeats();

		return heartbeats.size() > 0;
	}

	@Override
	protected void loadReports() {
		// do nothing
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			StateReport report = m_reportManager.getHourlyReport(getStartTime(), Constants.CAT, true);
			String ip = tree.getIpAddress();
			Machine machine = report.findOrCreateMachine(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());

			machine.findOrCreateProcessDomain(domain).addIp(ip);

			Project project = m_projectService.findProject(domain);

			if (project == null) {
				m_projectService.insert(domain);
			}
		}
	}

}
