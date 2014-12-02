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

public class ProblemReportFilter extends BaseVisitor {

	private int m_maxUrlSize = 100;

	public ProblemReportFilter() {

	}

	public ProblemReportFilter(int size) {
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
		List<Entry> errorCodes = new ArrayList<Entry>();

		for (Entry e : entries) {
			String status = e.getStatus();
			int length = status.length();

			for (int i = 0; i < length; i++) {
				// invalidate char
				if (status.charAt(i) > 126 || status.charAt(i) < 32) {
					errorCodes.add(e);
					break;
				}
			}

			if (ProblemType.LONG_URL.getName().equals(e.getType())) {
				longUrls.add(e);
			}
		}

		for (int i = 0; i < errorCodes.size(); i++) {
			entries.remove(errorCodes.get(i));
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
