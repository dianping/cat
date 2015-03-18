package com.dianping.cat.report.page.problem.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.DailyGraph;

public class ProblemDailyGraphCreator extends BaseVisitor {

	private String m_currentIp;

	private String m_currentType;

	private String m_currentName;

	private MachineInfo m_allMachine = new MachineInfo("All");

	private Map<String, MachineInfo> m_machines = new HashMap<String, MachineInfo>();

	private List<DailyGraph> m_dailyGraphs = new ArrayList<DailyGraph>();

	public List<DailyGraph> buildDailyGraph() {
		return m_dailyGraphs;
	}

	private DailyGraph buildDailyGraph(ProblemReport problemReport, MachineInfo info) {
		DailyGraph graph = new DailyGraph();
		StringBuilder summary = new StringBuilder();
		StringBuilder detail = new StringBuilder();

		graph.setDomain(problemReport.getDomain());
		graph.setPeriod(problemReport.getStartTime());
		graph.setName(ProblemAnalyzer.ID);
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

	private void buildMachineDetail(int count, MachineInfo info) {
		Integer type = info.getTypesCount().get(m_currentType);
		Integer name = info.getNameCount().get(m_currentType + "\t" + m_currentName);

		if (type == null) {
			type = count;
		} else {
			type = type + count;
		}
		info.getTypesCount().put(m_currentType, type);

		if (name == null) {
			name = count;
		} else {
			name = name + count;
		}
		info.getNameCount().put(m_currentType + "\t" + m_currentName, name);
	}

	public MachineInfo findOrCreateMachine(String ip) {
		MachineInfo info = m_machines.get(ip);

		if (info == null) {
			info = new MachineInfo(ip);
			m_machines.put(ip, info);
		}
		return info;
	}

	@Override
	public void visitDuration(Duration duration) {
		int count = duration.getCount();
		MachineInfo info = findOrCreateMachine(m_currentIp);

		buildMachineDetail(count, info);
		buildMachineDetail(count, m_allMachine);
	}

	@Override
	public void visitEntity(Entity entity) {
		m_currentType = entity.getType();
		m_currentName = entity.getStatus();

		super.visitEntity(entity);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentIp = machine.getIp();

		super.visitMachine(machine);
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);
		m_dailyGraphs.add(buildDailyGraph(problemReport, m_allMachine));

		for (MachineInfo info : m_machines.values()) {
			DailyGraph graph = buildDailyGraph(problemReport, info);

			m_dailyGraphs.add(graph);
		}
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

		public Map<String, Integer> getNameCount() {
			return m_nameCount;
		}

		public Map<String, Integer> getTypesCount() {
			return m_typesCount;
		}

		public void setNameCount(Map<String, Integer> nameCount) {
			m_nameCount = nameCount;
		}

		public void setTypesCount(Map<String, Integer> typesCount) {
			m_typesCount = typesCount;
		}
	}

}
