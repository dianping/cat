package com.dianping.cat.consumer.core.problem;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.core.aggregation.AggregationConfigManager;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportAggregation extends BaseVisitor {

	@Inject
	private AggregationConfigManager m_aggregationManger;

	private ProblemReport m_report;

	private Machine m_currentMachine;

	private Entry m_currentEntry;

	private JavaThread m_currentThread;

	private String m_domain;

	private static final int SIZE = 60;
	
	public void refreshRule(){
		m_aggregationManger.refreshRule();
	}

	public void setRuleManger(AggregationConfigManager ruleManger) {
		m_aggregationManger = ruleManger;
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		m_domain = problemReport.getDomain();
		m_report = new ProblemReport();
		m_report.getIps().addAll(problemReport.getIps());
		m_report.getDomainNames().addAll(problemReport.getDomainNames());
		m_report.setDomain(problemReport.getDomain());
		m_report.setStartTime(problemReport.getStartTime());
		m_report.setEndTime(problemReport.getEndTime());
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentMachine = m_report.findOrCreateMachine(machine.getIp());
		super.visitMachine(machine);
	}

	@Override
	public void visitEntry(Entry entry) {
		String type = entry.getType();
		String status = entry.getStatus();
		status = m_aggregationManger.handle(AggregationConfigManager.PROBLEM_TYPE, m_domain, status);
		m_currentEntry = findOrCreatEntry(m_currentMachine, type, status);
		super.visitEntry(entry);
	}

	@Override
	public void visitDuration(Duration duration) {
		int value = duration.getValue();
		Duration temp = m_currentEntry.findOrCreateDuration(value);
		mergeDuration(temp, duration);
	}

	protected void mergeDuration(Duration old, Duration duration) {
		old.setValue(duration.getValue());
		old.setCount(old.getCount() + duration.getCount());
		List<String> messages = old.getMessages();
		if (messages.size() < SIZE) {
			messages.addAll(duration.getMessages());
			if (messages.size() > SIZE) {
				messages = messages.subList(0, SIZE);
			}
		}
	}

	@Override
	public void visitThread(JavaThread thread) {
		m_currentThread = m_currentEntry.findOrCreateThread(thread.getId());
		super.visitThread(thread);
	}

	@Override
	public void visitSegment(Segment segment) {
		m_currentThread.addSegment(segment);
	}

	protected Entry findOrCreatEntry(Machine machine, String type, String status) {
		List<Entry> entries = machine.getEntries();

		for (Entry entry : entries) {
			if (entry.getType().equals(type) && entry.getStatus().equals(status)) {
				return entry;
			}
		}
		Entry entry = new Entry();
		entry.setStatus(status);
		entry.setType(type);
		entries.add(entry);
		return entry;
	}

	public ProblemReport getReport() {
		return m_report;
	}
}
