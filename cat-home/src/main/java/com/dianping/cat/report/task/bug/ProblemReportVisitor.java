package com.dianping.cat.report.task.bug;

import java.util.List;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.entity.Domain;
import com.dianping.cat.home.bug.entity.ExceptionItem;

public class ProblemReportVisitor extends BaseVisitor {

	private BugReport m_report;

	private String m_currentDomain;

	private String m_exception;

	@Override
	public void visitDuration(Duration duration) {
		int count = duration.getCount();
		List<String> messages = duration.getMessages();
		Domain domainInfo = m_report.findOrCreateDomain(m_currentDomain);
		ExceptionItem target = domainInfo.findOrCreateExceptionItem(m_exception);
		List<String> oldMessages = target.getMessages();

		target.setCount(target.getCount() + count);
		oldMessages.addAll(messages);
		if (oldMessages.size() > 10) {
			oldMessages = oldMessages.subList(0, 10);
		}
	}

	@Override
	public void visitEntry(Entry entry) {
		String type = entry.getType();

		if (ProblemType.ERROR.equals(type)) {
			m_exception = entry.getStatus();
			super.visitEntry(entry);
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		super.visitMachine(machine);
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		m_currentDomain = problemReport.getDomain();
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}

	public BugReport getReport() {
		return m_report;
	}

	public ProblemReportVisitor setReport(BugReport report) {
		m_report = report;
		return this;
	}

}
