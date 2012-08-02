package com.dianping.cat.notify.report;

import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;

public class ProblemReportMerger extends DefaultMerger {
	private static final int SIZE = 60;

	public ProblemReportMerger(ProblemReport problemReport) {
		super(problemReport);
	}

	protected Entry findEntry(Machine machine, Entry entry) {
		String type = entry.getType();
		String status = entry.getStatus();

		for (Entry e : machine.getEntries()) {
			if (e.getType().equals(type) && e.getStatus().equals(status)) {
				return e;
			}
		}

		return null;
	}

	@Override
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
	protected void mergeSegment(Segment old, Segment segment) {
		old.setCount(old.getCount() + segment.getCount());
		List<String> messages = old.getMessages();
		if (messages.size() < SIZE) {
			messages.addAll(segment.getMessages());
			if (messages.size() > SIZE) {
				messages = messages.subList(0, SIZE);
			}
		}
	}

	@Override
	public void visitEntry(Entry entry) {
		Machine machine = (Machine) getObjects().peek();
		Entry old = findEntry(machine, entry);

		if (old == null) {
			old = new Entry();
			old.setType(entry.getType()).setStatus(entry.getStatus());

			machine.addEntry(old);
		}

		visitEntryChildren(old, entry);
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);

		getProblemReport().getIps().addAll(problemReport.getIps());
		getProblemReport().getDomainNames().addAll(
				problemReport.getDomainNames());
	}
}
