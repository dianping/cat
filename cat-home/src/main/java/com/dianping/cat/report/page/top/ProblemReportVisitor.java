package com.dianping.cat.report.page.top;

import java.text.SimpleDateFormat;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.report.page.top.DomainInfo.Metric;

public class ProblemReportVisitor extends BaseVisitor {

	private DomainInfo m_info;

	private String m_ipAddress;

	private String m_type;

	private String m_date;

	private Integer m_minute;

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:");

		m_date = sdf.format(problemReport.getStartTime());
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitMachine(Machine machine) {
		String id = machine.getIp();

		if (Constants.ALL.equals(m_ipAddress) || id.equals(m_ipAddress)) {
			super.visitMachine(machine);
		}
	}

	public ProblemReportVisitor(String ipAddress, DomainInfo info, String type) {
		m_info = info;
		m_type = type;
		m_ipAddress = ipAddress;
	}

	@Override
	public void visitEntity(Entity entity) {
		if (m_type.equals(entity.getType())) {
			super.visitEntity(entity);
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		m_minute = segment.getId();
		int count = segment.getCount();
		String key = "";

		if (m_minute >= 10) {
			key = m_date + m_minute;
		} else {
			key = m_date + '0' + m_minute;
		}
		Metric metric = m_info.getMetric(key);

		metric.addException(count);
	}

}