package com.dianping.cat.report.page.statistics.task.bug;

import java.util.List;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.entity.Domain;
import com.dianping.cat.home.bug.entity.ExceptionItem;

public class ProblemReportVisitor extends BaseVisitor {

	private BugReport m_report;

	private String m_currentDomain;

	private String m_exception;

	private int SIZE = 10;

	public BugReport getReport() {
		return m_report;
	}

	protected void mergeList(List<String> oldMessages, List<String> newMessages, int size) {
		int originalSize = oldMessages.size();

		if (originalSize < size) {
			int remainingSize = size - originalSize;

			if (remainingSize >= newMessages.size()) {
				oldMessages.addAll(newMessages);
			} else {
				oldMessages.addAll(newMessages.subList(0, remainingSize));
			}
		}
	}

	public ProblemReportVisitor setReport(BugReport report) {
		m_report = report;
		return this;
	}

	@Override
	public void visitDuration(Duration duration) {
		int count = duration.getCount();
		Domain domainInfo = m_report.findOrCreateDomain(m_currentDomain);
		ExceptionItem target = domainInfo.findOrCreateExceptionItem(m_exception);
		List<String> oldMessages = target.getMessages();
		List<String> newMessages = duration.getMessages();

		target.setCount(target.getCount() + count);
		mergeList(oldMessages, newMessages, SIZE);
	}

	@Override
	public void visitEntity(Entity entity) {
		String type = entity.getType();

		if (ProblemType.ERROR.getName().equals(type)) {
			m_exception = entity.getStatus();
			super.visitEntity(entity);
		}
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		m_currentDomain = problemReport.getDomain();
		super.visitProblemReport(problemReport);
	}

}
