package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.helper.CatString;

public class StateShow extends BaseVisitor {

	private Machine m_total = new Machine();

	private Map<Long, Message> m_messages = new LinkedHashMap<Long, Message>();

	private List<ProcessDomain> m_processDomains = new ArrayList<ProcessDomain>();

	private String m_ip;

	public Machine getTotal() {
		return m_total;
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

	public List<ProcessDomain> getProcessDomains() {
		return m_processDomains;
	}

	public StateShow(String ip) {
		m_ip = ip;
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();
		if (m_total == null) {
			m_total = new Machine();
			m_total.setIp(ip);
		}
		if (m_ip.equals(CatString.ALL_IP) || m_ip.equalsIgnoreCase(ip)) {
			m_total = mergerMachine(m_total, machine);
		}
		super.visitMachine(machine);
	}

	@Override
	public void visitMessage(Message message) {
		Message temp = m_messages.get(message.getId());
		if (temp == null) {
			m_messages.put(message.getId(), temp);
		} else {
			mergerMessage(temp, message);
		}
	}

	private Machine mergerMachine(Machine total, Machine machine) {
		double oldCount = 0;
		double newCount = 0;
		if (total.getAvgTps() > 0) {
			oldCount = total.getTotal() / total.getAvgTps();
		}
		if (machine.getAvgTps() > 0) {
			newCount = machine.getTotal() / machine.getAvgTps();
		}
		double totalCount = oldCount + newCount;
		if (totalCount > 0) {
			total.setAvgTps((total.getTotal() + machine.getTotal()) / totalCount);
		}

		total.setTotal(total.getTotal() + machine.getTotal());
		total.setTotalLoss(total.getTotalLoss() + machine.getTotalLoss());
		total.setDump(total.getDump() + machine.getDump());
		total.setDumpLoss(total.getDumpLoss() + machine.getDumpLoss());
		total.setSize(total.getSize() + machine.getSize());
		total.setDelaySum(total.getDelaySum() + machine.getDelaySum());
		total.setDelayCount(total.getDelayCount() + machine.getDelayCount());

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
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		m_processDomains.add(processDomain);
	}

	@Override
	public void visitStateReport(StateReport stateReport) {
		super.visitStateReport(stateReport);
	}
}
