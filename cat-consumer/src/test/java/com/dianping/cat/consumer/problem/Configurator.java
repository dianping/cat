package com.dianping.cat.consumer.problem;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return ProblemAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = ProblemAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockProblemReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedProblemDelegate.class));

		return all;
	}

	public static class ExtendedProblemDelegate extends ProblemDelegate {
	}

	public static class MockProblemReportManager extends MockReportManager<ProblemReport> {
		private ProblemReport m_report;

		@Inject
		private ReportDelegate<ProblemReport> m_delegate;

		@Override
		public ProblemReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (ProblemReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		public void setReport(ProblemReport report) {
      	m_report = report;
      }

		@Override
      public void destory() {
      }
	}
}
