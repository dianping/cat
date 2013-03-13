package com.dianping.cat.report.task.problem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.home.dal.report.Dailygraph;

public class ProblemDailyGraphCreator extends BaseVisitor {

	private String m_currentIp;

	private String m_currentType;

	private String m_currentName;

	private MachineInfo m_allMachine = new MachineInfo("All");

	private Map<String, MachineInfo> m_machines = new HashMap<String, MachineInfo>();

	private List<Dailygraph> m_dailyGraphs = new ArrayList<Dailygraph>();

	public MachineInfo findOrCreateMachine(String ip) {
		MachineInfo info = m_machines.get(ip);

		if (info == null) {
			info = new MachineInfo(ip);
			m_machines.put(ip, info);
		}
		return info;
	}

	@Override
	public void visitEntry(Entry entry) {
		m_currentType = entry.getType();
		m_currentName = entry.getStatus();
		super.visitEntry(entry);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentIp = machine.getIp();
		super.visitMachine(machine);
	}

	@Override
	public void visitDuration(Duration duration) {
		int count = duration.getCount();
		MachineInfo info = findOrCreateMachine(m_currentIp);
		buildMachineDetail(count, info);
		buildMachineDetail(count, m_allMachine);
	}

	private void buildMachineDetail(int count, MachineInfo info) {
		Integer type = info.getTypesCount().get(m_currentType);
		Integer name = info.getNameCount().get(m_currentType + "\t" + m_currentName);

		if (type == null) {
			type = new Integer(count);
		} else {
			type = type + count;
		}
		info.getTypesCount().put(m_currentType, type);

		if (name == null) {
			name = new Integer(count);
		} else {
			name = name + count;
		}
		info.getNameCount().put(m_currentType + "\t" + m_currentName, name);
	}

	public List<Dailygraph> buildDailyGraph() {
		return m_dailyGraphs;
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);
		m_dailyGraphs.add(buildDailyGraph(problemReport, m_allMachine));

		for (MachineInfo info : m_machines.values()) {
			Dailygraph graph = buildDailyGraph(problemReport, info);

			m_dailyGraphs.add(graph);
		}
	}

	private Dailygraph buildDailyGraph(ProblemReport problemReport, MachineInfo info) {
		Dailygraph graph = new Dailygraph();
		StringBuilder summary = new StringBuilder();
		StringBuilder detail = new StringBuilder();

		graph.setDomain(problemReport.getDomain());
		graph.setPeriod(problemReport.getStartTime());
		graph.setName("problem");
		graph.setIp(info.getIp());
		graph.setType(3);
		graph.setCreationDate(new Date());

		for (java.util.Map.Entry<String, Integer> type : info.getTypesCount().entrySet()) {
			summary.append(type.getKey() + '\t' + type.getValue() + '\n');
		}

		for (java.util.Map.Entry<String, Integer> name : info.getNameCount().entrySet()) {
			detail.append(name.getKey() + '\t' + name.getValue() + '\n');
		}
		graph.setSummaryContent(summary.toString());
		graph.setDetailContent(detail.toString());
		return graph;
	}

	public static class MachineInfo {
		private String m_ip;

		private Map<String, Integer> m_typesCount = new HashMap<String, Integer>();

		private Map<String, Integer> m_nameCount = new HashMap<String, Integer>();

		public MachineInfo(String ip) {
			m_ip = ip;
		}

		public String getIp() {
			return m_ip;
		}

		public Map<String, Integer> getTypesCount() {
			return m_typesCount;
		}

		public void setTypesCount(Map<String, Integer> typesCount) {
			m_typesCount = typesCount;
		}

		public Map<String, Integer> getNameCount() {
			return m_nameCount;
		}

		public void setNameCount(Map<String, Integer> nameCount) {
			m_nameCount = nameCount;
		}
	}

}
