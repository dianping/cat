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
		List<String> oldMessages = old.getMessages();
		List<String> newMessages = duration.getMessages();

		old.setValue(duration.getValue());
		old.setCount(old.getCount() + duration.getCount());

		mergeList(oldMessages, newMessages, SIZE);
	}

	protected List<String> mergeList(List<String> oldMessages, List<String> newMessages, int size) {
		int originalSize = oldMessages.size();
		
		if (originalSize < size) {
			int remainingSize = size - originalSize;

			if (remainingSize >= newMessages.size()) {
				oldMessages.addAll(newMessages);
			} else {
				oldMessages.addAll(newMessages.subList(0, remainingSize));
			}
		}
		return oldMessages;
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		List<String> oldMessages = old.getMessages();
		List<String> newMessages = segment.getMessages();

		old.setCount(old.getCount() + segment.getCount());
		mergeList(oldMessages, newMessages, SIZE);
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
