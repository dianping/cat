package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.helper.MapUtils;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportService;

public class ExceptionAnalyzer extends ComponentTestCase {

	private ProjectDao m_projectDao;

	@Test
	public void test() throws Exception {
		ReportService reportService = lookup(ReportService.class);
		m_projectDao = lookup(ProjectDao.class);
		String date = "2013-07-14";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date start = sdf.parse(date);
		ProblemReport problem = reportService.queryProblemReport("All", start, new Date(start.getTime()
		      + TimeUtil.ONE_DAY));

		Visitor visitor = new Visitor();
		visitor.visitProblemReport(problem);

		Map<String, ErrorStatis> errors = visitor.getErrors();

		errors = MapUtils.sortMap(errors, new Comparator<java.util.Map.Entry<String, ErrorStatis>>() {

			@Override
			public int compare(java.util.Map.Entry<String, ErrorStatis> o1, java.util.Map.Entry<String, ErrorStatis> o2) {
				String department1 = o1.getValue().getDepartment();
				String department2 = o2.getValue().getDepartment();
				String productLine1 = o1.getValue().getProductLine();
				String productLine2 = o2.getValue().getProductLine();

				if (department1.equals(department2)) {
					return productLine1.compareTo(productLine2);
				}
				return department1.compareTo(department2);
			}
		});

		for (ErrorStatis temp : errors.values()) {

			System.out.println(temp.getDepartment() + "\t" + temp.getProductLine());
			Map<String, Integer> detail = temp.getErrors();

			detail = MapUtils.sortMap(detail, new Comparator<java.util.Map.Entry<String, Integer>>() {

				@Override
				public int compare(java.util.Map.Entry<String, Integer> o1, java.util.Map.Entry<String, Integer> o2) {
					return o2.getValue() - o1.getValue();
				}
			});

			for (java.util.Map.Entry<String, Integer> entry : detail.entrySet()) {
				System.out.println(entry.getKey() + "\t" + entry.getValue());
			}
		}
	}

	public Project findByDomain(String domain) {
		try {
			return m_projectDao.findByDomain(domain, ProjectEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	public static class ErrorStatis {
		private String m_productLine;

		private String m_department;

		private Map<String, Integer> m_errors = new HashMap<String, Integer>();

		public String getProductLine() {
			return m_productLine;
		}

		public void setProductLine(String productLine) {
			m_productLine = productLine;
		}

		public String getDepartment() {
			return m_department;
		}

		public void setDepartment(String department) {
			m_department = department;
		}

		public Map<String, Integer> getErrors() {
			return m_errors;
		}

		public void setErrors(Map<String, Integer> errors) {
			m_errors = errors;
		}
	}

	public class Visitor extends BaseVisitor {

		private String m_domain;

		private Map<String, ErrorStatis> m_errors = new HashMap<String, ErrorStatis>();

		public Map<String, ErrorStatis> getErrors() {
			return m_errors;
		}

		public ErrorStatis findOrCreateErrorStatis(String productLine) {
			ErrorStatis statis = m_errors.get(productLine);

			if (statis == null) {
				statis = new ErrorStatis();
				m_errors.put(productLine, statis);
			}

			return statis;
		}

		public void setErrors(Map<String, ErrorStatis> errors) {
			m_errors = errors;
		}

		@Override
		public void visitEntry(Entry entry) {
			String type = entry.getType();
			String status = entry.getStatus();

			if (type.equals("error")) {
				Map<Integer, Duration> durations = entry.getDurations();
				int count = 0;
				for (Duration duration : durations.values()) {
					count = count + duration.getCount();
				}

				Project project = findByDomain(m_domain);

				if (project != null) {
					String productLine = project.getProjectLine();
					ErrorStatis statis = findOrCreateErrorStatis(productLine);
					statis.setDepartment(project.getDepartment());
					statis.setProductLine(project.getProjectLine());
					Map<String, Integer> errors = statis.getErrors();

					Integer temp = errors.get(status);
					if (temp == null) {
						errors.put(status, count);
					} else {
						errors.put(status, temp + count);
					}
				}
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			m_domain = machine.getIp();
			super.visitMachine(machine);
		}

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			super.visitProblemReport(problemReport);
		}
	}
}
