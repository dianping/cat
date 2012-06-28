package com.dianping.cat.report.page.model.problem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;

public class ProblemReportMerger extends DefaultMerger {

	public ProblemReportMerger(ProblemReport problemReport) {
		super(problemReport);
	}

//	protected Entry findEntry(Machine machine, Entry entry) {
//		String type = entry.getType();
//		String status = entry.getStatus();
//
//		List<Entry> entries = machine.getEntries();
//
//		for (Entry temp : entries) {
//			if (temp.getType().equals(type) && temp.getStatus().equals(status)) {
//				return entry;
//			}
//		}
//		return null;
//	}
//
//	@Override
//	public void visitEntry(Entry entry) {
//		Machine machine1 = getProblemReport().getMachines().get("192.168.165.1");
//		Machine machine = (Machine) getObjects().peek();
//		Entry old = findEntry(machine, entry);
//		
//		if (old == null) {
//			old = new Entry();
//			old.setType(entry.getType());
//			old.setStatus(entry.getStatus());
//			System.out.println("add: " + entry.getType() + ":" + entry.getStatus());
//			machine.addEntry(old);
//		}
//
//		visitEntryChildren(old, entry);
//	}
//
//	@Override
//	protected void mergeDuration(Duration old, Duration duration) {
//		old.setValue(duration.getValue());
//		old.setCount(old.getCount() + duration.getCount());
//		old.getMessages().addAll(duration.getMessages());
//	}
//
//	@Override
//	protected void mergeThread(JavaThread old, JavaThread thread) {
//		super.mergeThread(old, thread);
//	}
//
//	@Override
//	protected void mergeSegment(Segment old, Segment segment) {
//		old.setCount(old.getCount() + segment.getCount());
//		old.getMessages().addAll(segment.getMessages());
//	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);

		getProblemReport().getIps().addAll(problemReport.getIps());
		getProblemReport().getDomainNames().addAll(problemReport.getDomainNames());
	}
}
