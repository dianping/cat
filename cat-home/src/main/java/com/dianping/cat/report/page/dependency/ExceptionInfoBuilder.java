package com.dianping.cat.report.page.dependency;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.MapUtils;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.dependency.graph.GraphConstrant;

public class ExceptionInfoBuilder extends BaseVisitor {

	private Map<String, Integer> m_errors = new LinkedHashMap<String, Integer>();

	private Date m_start;

	@Override
	public void visitEntry(Entry entry) {
		String type = entry.getType();
		String state = entry.getStatus();
		int count = 0;
		for (Duration duration : entry.getDurations().values()) {
			count += duration.getCount();
		}

		if ("error".equals(type)) {
			Integer temp = m_errors.get(state);

			if (temp == null) {
				m_errors.put(state, count);
			} else {
				m_errors.put(state, new Integer(temp + count));
			}
		}
	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		m_start = problemReport.getStartTime();
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}

	public String buildResult() {
		StringBuilder sb = new StringBuilder();
		Comparator<java.util.Map.Entry<String, Integer>> compator = new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(java.util.Map.Entry<String, Integer> arg0, java.util.Map.Entry<String, Integer> arg1) {
				return arg1.getValue() - arg0.getValue();
			}
		};
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (m_errors.size() > 0) {
			sb.append(GraphConstrant.LINE).append(GraphConstrant.ENTER);
			sb.append("<span style='color:red'>").append(CatString.EXCEPTION_INFO).append("（");
			sb.append(sdf.format(m_start)).append("-")
			      .append(sdf.format(new Date(m_start.getTime() + TimeUtil.ONE_HOUR - 1))).append("）");
			sb.append("</span>").append(GraphConstrant.ENTER);
		}
		m_errors = MapUtils.sortMap(m_errors, compator);
		for (java.util.Map.Entry<String, Integer> error : m_errors.entrySet()) {
			sb.append(error.getKey()).append(GraphConstrant.DELIMITER).append(error.getValue())
			      .append(GraphConstrant.ENTER);
		}
		return sb.toString();
	}

}
