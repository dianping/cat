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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;

public class StateDisplay extends BaseVisitor {

	protected String m_ip;

	protected ProcessDomain m_processDomain;

	protected StateReport m_stateReport = new StateReport();

	private String m_sortType;

	private Set<String> m_fakeDomains;

	public StateDisplay(String ip, Set<String> fakeDomains) {
		m_ip = ip;
		m_fakeDomains = fakeDomains;
	}

	public Machine getMachine() {
		return m_stateReport.findOrCreateMachine(m_ip);
	}

	public Map<Long, Message> getMessages() {
		return getMachine().getMessages();
	}

	public ProcessDomain getProcessDomain(String name) {
		return getMachine().findOrCreateProcessDomain(name);
	}

	public List<ProcessDomain> getProcessDomains() {
		List<ProcessDomain> domains = new ArrayList<ProcessDomain>(
								m_stateReport.findMachine(m_ip).getProcessDomains().values());

		if (m_sortType == null) {
			Collections.sort(domains, new SizeCompartor());
		} else if (m_sortType.equals("total")) {
			Collections.sort(domains, new TotalCompartor());
		} else if (m_sortType.equals("loss")) {
			Collections.sort(domains, new LossCompartor());
		} else if (m_sortType.equals("size")) {
			Collections.sort(domains, new SizeCompartor());
		} else if (m_sortType.equals("avg")) {
			Collections.sort(domains, new AvgCompartor());
		} else if (m_sortType.equals("machine")) {
			Collections.sort(domains, new MachineCompartor());
		} else {
			Collections.sort(domains, new DomainCompartor());
		}
		return domains;
	}

	public StateReport getStateReport() {
		return m_stateReport;
	}

	public int getTotalSize() {
		Set<String> ips = new HashSet<String>();

		for (ProcessDomain process : getMachine().getProcessDomains().values()) {
			Set<String> temp = process.getIps();

			for (String ip : temp) {
				if (validateIp(ip)) {
					ips.add(ip);
				}
			}
		}
		return ips.size();
	}

	protected Detail mergeDetail(ProcessDomain processDomain, Detail other) {
		Detail old = processDomain.findOrCreateDetail(other.getId());

		old.setSize(old.getSize() + other.getSize());
		old.setTotal(old.getTotal() + other.getTotal());
		old.setTotalLoss(old.getTotalLoss() + other.getTotalLoss());
		return old;
	}

	protected ProcessDomain mergeProcessDomain(ProcessDomain other, String name) {
		ProcessDomain old = getMachine().findOrCreateProcessDomain(name);

		old.setSize(old.getSize() + other.getSize());
		old.setTotal(old.getTotal() + other.getTotal());
		old.setTotalLoss(old.getTotalLoss() + other.getTotalLoss());
		old.setAvg(old.getTotal() > 0 ? old.getSize() / old.getTotal() : 0);
		return old;
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

	public void setSortType(String sort) {
		m_sortType = sort;
	}

	public boolean validateIp(String str) {
		Pattern pattern = Pattern
								.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

	@Override
	public void visitDetail(Detail detail) {
		mergeDetail(m_processDomain, detail);
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();

		if (m_ip.equals(Constants.ALL) || m_ip.equalsIgnoreCase(ip)) {
			Machine m = getMachine();

			mergerMachine(m, machine);
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitMessage(Message message) {
		Message total = getMachine().findOrCreateMessage(message.getId());

		total.setDelayCount(total.getDelayCount() + message.getDelayCount());
		total.setDelaySum(total.getDelaySum() + message.getDelaySum());
		total.setDump(total.getDump() + message.getDump());
		total.setDumpLoss(total.getDumpLoss() + message.getDumpLoss());
		total.setSize(total.getSize() + message.getSize());
		total.setTotal(total.getTotal() + message.getTotal());
		total.setTotalLoss(total.getTotalLoss() + message.getTotalLoss());
		total.setBlockTotal(total.getBlockTotal() + message.getBlockTotal());
		total.setBlockLoss(total.getBlockLoss() + message.getBlockLoss());
		total.setBlockTime(total.getBlockTime() + message.getBlockTime());
		total.setPigeonTimeError(total.getPigeonTimeError() + message.getPigeonTimeError());
		total.setNetworkTimeError(total.getNetworkTimeError() + message.getNetworkTimeError());
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		m_processDomain = mergeProcessDomain(processDomain, processDomain.getName());

		for (String ip : processDomain.getIps()) {
			if (!m_fakeDomains.contains(ip)) {
				m_processDomain.getIps().add(ip);
			}
		}
		super.visitProcessDomain(processDomain);
	}

	@Override
	public void visitStateReport(StateReport stateReport) {
		m_stateReport.setDomain(stateReport.getDomain()).setStartTime(stateReport.getStartTime())
								.setEndTime(stateReport.getEndTime());
		super.visitStateReport(stateReport);
	}

	public static class AvgCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			if (o1.getName() != null && o1.getName().equalsIgnoreCase(Constants.ALL)) {
				return -1;
			}
			if (o2.getName() != null && o2.getName().equalsIgnoreCase(Constants.ALL)) {
				return 1;
			}
			return (int) (o2.getAvg() * 100 - o1.getAvg() * 100);
		}
	}

	public static class DomainCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			if (o1.getName() != null && o1.getName().equalsIgnoreCase(Constants.ALL)) {
				return -1;
			}
			if (o2.getName() != null && o2.getName().equalsIgnoreCase(Constants.ALL)) {
				return 1;
			}
			return o1.getName().compareTo(o2.getName());
		}
	}

	public static class LossCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			if (o1.getName() != null && o1.getName().equalsIgnoreCase(Constants.ALL)) {
				return -1;
			}
			if (o2.getName() != null && o2.getName().equalsIgnoreCase(Constants.ALL)) {
				return 1;
			}
			return (int) (o2.getTotalLoss() - o1.getTotalLoss());
		}
	}

	public static class MachineCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			if (o1.getName() != null && o1.getName().equalsIgnoreCase(Constants.ALL)) {
				return -1;
			}
			if (o2.getName() != null && o2.getName().equalsIgnoreCase(Constants.ALL)) {
				return 1;
			}
			return (int) (o2.getIps().size() - o1.getIps().size());
		}
	}

	public static class SizeCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			if (o1.getName() != null && o1.getName().equalsIgnoreCase(Constants.ALL)) {
				return -1;
			}
			if (o2.getName() != null && o2.getName().equalsIgnoreCase(Constants.ALL)) {
				return 1;
			}
			return new Double(o2.getSize()).compareTo(o1.getSize());
		}
	}

	public static class TotalCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			if (o1.getName() != null && o1.getName().equalsIgnoreCase(Constants.ALL)) {
				return -1;
			}
			if (o2.getName() != null && o2.getName().equalsIgnoreCase(Constants.ALL)) {
				return 1;
			}

			long count2 = o2.getTotal();
			long count1 = o1.getTotal();

			if (count2 > count1) {
				return 1;
			} else if (count2 < count1) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}
