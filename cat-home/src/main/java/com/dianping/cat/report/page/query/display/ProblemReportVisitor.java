package com.dianping.cat.report.page.query.display;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;

public class ProblemReportVisitor extends BaseVisitor {
	private String m_type;

	private String m_name;

	private String m_currentType;

	private String m_currentName;

	public ProblemQueryItem m_item = new ProblemQueryItem();

	public ProblemReportVisitor(String type, String name) {
		m_type = type;
		m_name = name;
		m_item.setType(type);
		m_item.setName(name);
	}

	public ProblemQueryItem getItem() {
		return m_item;
	}

	public void setItem(ProblemQueryItem item) {
		m_item = item;
	}

	@Override
	public void visitDuration(Duration duration) {
		long count = duration.getCount();

		if (m_name == null || m_name.trim().length() == 0) {
			if (m_type.equals(m_currentType)) {
				m_item.addCount(count);
			}
		} else {
			if (m_type.equals(m_currentType) && m_name.equals(m_currentName)) {
				m_item.addCount(count);
			}
		}
	}

	@Override
	public void visitEntry(Entry entry) {
		m_currentType = entry.getType();
		m_currentName = entry.getStatus();
		for (Duration duration : entry.getDurations().values()) {
			visitDuration(duration);
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		super.visitMachine(machine);
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);
		m_item.setDate(problemReport.getStartTime());
	}

}
