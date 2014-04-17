package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.model.entity.Detail;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;

public class StateShow extends BaseVisitor {

	private Machine m_total = new Machine();

	private Map<Long, Message> m_messages = new LinkedHashMap<Long, Message>();

	private Map<String, ProcessDomain> m_processDomains = new LinkedHashMap<String, ProcessDomain>();

	private String m_currentIp;

	private String m_ip;

	private String m_sortType;

	private ProcessDomain m_processDomain;

	public StateShow(String ip) {
		m_ip = ip;
	}

	public List<Message> getMessages() {
		List<Message> all = new ArrayList<Message>(m_messages.values());
		List<Message> result = new ArrayList<Message>();
		long current = System.currentTimeMillis();

		for (Message message : all) {
			if (message.getId() < current) {
				result.add(message);
			}
		}
		return result;
	}

	public Map<Long, Message> getMessagesMap() {
		return m_messages;
	}

	public Map<String, ProcessDomain> getProcessDomainMap() {
		return m_processDomains;
	}

	public List<ProcessDomain> getProcessDomains() {
		ProcessDomain domain = m_processDomains.get("PhoenixAgent");

		domain.getIps().clear();

		List<ProcessDomain> temp = new ArrayList<ProcessDomain>(m_processDomains.values());
		if (m_sortType == null) {
			Collections.sort(temp, new DomainCompartor());
		} else if (m_sortType.equals("total")) {
			Collections.sort(temp, new TotalCompartor());
		} else if (m_sortType.equals("loss")) {
			Collections.sort(temp, new LossCompartor());
		} else if (m_sortType.equals("size")) {
			Collections.sort(temp, new SizeCompartor());
		} else {
			Collections.sort(temp, new DomainCompartor());
		}

		temp.add(0, mergeAll(temp));
		return temp;
	}

	public Machine getTotal() {
		return m_total;
	}

	public int getTotalSize() {
		Set<String> ips = new HashSet<String>();

		for (ProcessDomain domain : m_processDomains.values()) {
			Set<String> temp = domain.getIps();

			for (String str : temp) {
				if (validateIp(str)) {
					ips.add(str);
				}
			}
		}
		return ips.size();
	}

	private ProcessDomain mergeAll(List<ProcessDomain> domains) {
		ProcessDomain all = new ProcessDomain("ALL");

		for (ProcessDomain temp : domains) {
			all.setSize(all.getSize() + temp.getSize());
			all.setTotal(all.getTotal() + temp.getTotal());
			all.setTotalLoss(all.getTotalLoss() + temp.getTotalLoss());
		}
		return all;
	}

	private Machine mergerMachine(Machine total, Machine machine) {
		total.setAvgTps(total.getAvgTps() + machine.getAvgTps());
		total.setTotal(total.getTotal() + machine.getTotal());
		total.setTotalLoss(total.getTotalLoss() + machine.getTotalLoss());
		total.setDump(total.getDump() + machine.getDump());
		total.setDumpLoss(total.getDumpLoss() + machine.getDumpLoss());
		total.setSize(total.getSize() + machine.getSize());
		total.setDelaySum(total.getDelaySum() + machine.getDelaySum());
		total.setDelayCount(total.getDelayCount() + machine.getDelayCount());
		total.setBlockTotal(total.getBlockTotal() + machine.getBlockTotal());
		total.setBlockLoss(total.getBlockLoss() + machine.getBlockLoss());
		total.setBlockTime(total.getBlockTime() + machine.getBlockTime());
		total.setPigeonTimeError(total.getPigeonTimeError() + machine.getPigeonTimeError());
		total.setNetworkTimeError(total.getNetworkTimeError() + machine.getNetworkTimeError());

		if (machine.getMaxTps() > total.getMaxTps()) {
			total.setMaxTps(machine.getMaxTps());
		}

		long count = total.getDelayCount();
		double sum = total.getDelaySum();
		if (count > 0) {
			total.setDelayAvg(sum / count);
		}

		return total;
	}

	private void mergerMessage(Message total, Message message) {
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
		Map<Long, Detail> details = m_processDomain.getDetails();
		Long id = detail.getId();
		Detail temp = details.get(id);
		if (temp == null) {
			details.put(id, detail);
		} else {
			temp.setSize(temp.getSize() + detail.getSize());
			temp.setTotal(temp.getTotal() + detail.getTotal());
			temp.setTotalLoss(temp.getTotalLoss() + detail.getTotalLoss());
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();
		m_currentIp = ip;

		if (m_total == null) {
			m_total = new Machine();
			m_total.setIp(ip);
		}
		if (m_ip.equals(Constants.ALL) || m_ip.equalsIgnoreCase(ip)) {
			m_total = mergerMachine(m_total, machine);
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitMessage(Message message) {
		Message temp = m_messages.get(message.getId());
		if (temp == null) {
			m_messages.put(message.getId(), message);
		} else {
			mergerMessage(temp, message);
		}
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		if (m_ip.equals(m_currentIp) || m_ip.equals(Constants.ALL)) {
			m_processDomain = m_processDomains.get(processDomain.getName());
			if (m_processDomain == null) {
				m_processDomains.put(processDomain.getName(), processDomain);
			} else {
				m_processDomain.getIps().addAll(processDomain.getIps());
				m_processDomain.setSize(m_processDomain.getSize() + processDomain.getSize());
				m_processDomain.setTotal(m_processDomain.getTotal() + processDomain.getTotal());
				m_processDomain.setTotalLoss(m_processDomain.getTotalLoss() + processDomain.getTotalLoss());
				super.visitProcessDomain(processDomain);
			}
		}
	}

	public static class DomainCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}

	public static class LossCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			return (int) (o2.getTotalLoss() - o1.getTotalLoss());
		}
	}

	public static class SizeCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			return new Double(o2.getSize()).compareTo(o1.getSize());
		}
	}

	public static class TotalCompartor implements Comparator<ProcessDomain> {

		@Override
		public int compare(ProcessDomain o1, ProcessDomain o2) {
			return (int) (o2.getTotal() - o1.getTotal());
		}
	}
}
