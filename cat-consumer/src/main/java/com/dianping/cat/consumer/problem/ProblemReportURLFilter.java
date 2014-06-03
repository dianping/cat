package com.dianping.cat.consumer.problem;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportURLFilter extends BaseVisitor {

	private int m_maxUrlSize = 50;

	public ProblemReportURLFilter() {

	}

	public ProblemReportURLFilter(int size) {
		m_maxUrlSize = size;
	}

	@Override
	public void visitDuration(Duration duration) {
		super.visitDuration(duration);
	}

	@Override
	public void visitEntry(Entry entry) {
		super.visitEntry(entry);
	}

	@Override
	public void visitMachine(Machine machine) {
		List<Entry> entries = machine.getEntries();
		List<Entry> longUrls = new ArrayList<Entry>();

		for (Entry e : entries) {
			String type = e.getType();

			if (ProblemType.LONG_URL.getName().equals(type)) {
				longUrls.add(e);
			}
		}

		int size = longUrls.size();

		if (size > m_maxUrlSize) {
			for (int i = m_maxUrlSize; i < size; i++) {
				entries.remove(longUrls.get(i));
			}
		}
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}

	@Override
	public void visitThread(JavaThread thread) {
		super.visitThread(thread);
	}

}
