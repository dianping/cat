package com.dianping.cat.consumer.problem;

import java.util.List;
import java.util.Stack;

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

	protected Entry findOrCreateEntry(Machine machine, Entry entry) {
		String type = entry.getType();
		String status = entry.getStatus();

		for (Entry e : machine.getEntries()) {
			if (e.getType().equals(type) && e.getStatus().equals(status)) {
				return e;
			}
		}

		Entry result = new Entry();

		result.setStatus(status).setType(type);
		machine.addEntry(result);
		return result;
	}

	@Override
	protected void mergeDuration(Duration old, Duration duration) {
		List<String> messages = old.getMessages();

		old.setValue(duration.getValue());
		old.setCount(old.getCount() + duration.getCount());
		if (messages.size() < SIZE) {
			messages.addAll(duration.getMessages());
			if (messages.size() > SIZE) {
				messages = messages.subList(0, SIZE);
			}
		}
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		List<String> messages = old.getMessages();

		old.setCount(old.getCount() + segment.getCount());
		if (messages.size() < SIZE) {
			messages.addAll(segment.getMessages());
			if (messages.size() > SIZE) {
				messages = messages.subList(0, SIZE);
			}
		}
	}

	@Override
	protected void visitMachineChildren(Machine to, Machine from) {
		Stack<Object> objs = getObjects();

		for (Entry source : from.getEntries()) {
			Entry target = findOrCreateEntry(to, source);

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);

		getProblemReport().getIps().addAll(problemReport.getIps());
		getProblemReport().getDomainNames().addAll(problemReport.getDomainNames());
	}
}
