package com.dianping.cat.consumer.core;

import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportAllBuilder  extends BaseVisitor {

	private ProblemReport m_report;

	private String m_currentDomain;

	private String m_currentType;

	private String m_currentState;

	private String m_currentThread;

	public ProblemReportAllBuilder(ProblemReport report) {
		m_report = report;
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

	@Override
	public void visitDuration(Duration duration) {
		int value = duration.getValue();
		Machine machine = m_report.findOrCreateMachine(m_currentDomain);
		Entry entry = findOrCreatEntry(machine, m_currentType, m_currentState);
		Duration temp = entry.findOrCreateDuration(value);

		temp.setCount(temp.getCount() + duration.getCount());
	}

	@Override
	public void visitEntry(Entry entry) {
		m_currentType = entry.getType();
		m_currentState = entry.getStatus();
		super.visitEntry(entry);
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		m_currentDomain = problemReport.getDomain();
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitMachine(Machine machine) {
		super.visitMachine(machine);
	}

	@Override
	public void visitSegment(Segment segment) {
		int minute = segment.getId();
		int count = segment.getCount();
		Machine machine = m_report.findOrCreateMachine(m_currentDomain);
		Entry entry = findOrCreatEntry(machine, m_currentType, m_currentState);
		JavaThread thread = entry.findOrCreateThread(m_currentThread);
		Segment temp = thread.findOrCreateSegment(minute);

		temp.setCount(temp.getCount() + count);
	}

	@Override
	public void visitThread(JavaThread thread) {
		m_currentThread = thread.getId();
		super.visitThread(thread);
	}
}