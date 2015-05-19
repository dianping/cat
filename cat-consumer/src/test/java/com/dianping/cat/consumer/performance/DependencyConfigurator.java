package com.dianping.cat.consumer.performance;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class DependencyConfigurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new DependencyConfigurator());
	}

	protected Class<?> getTestClass() {
		return DependencyPerformanceTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = DependencyAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockDependencyReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedDependencyDelegate.class));

		return all;
	}

	public static class ExtendedDependencyDelegate extends DependencyDelegate {
	}

	public static class MockDependencyReportManager extends MockReportManager<DependencyReport> {
		private DependencyReport m_report;

		@Inject
		private ReportDelegate<DependencyReport> m_delegate;

		@Override
		public DependencyReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (DependencyReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}
			
			return m_report;
		}

		public void setReport(DependencyReport report) {
      	m_report = report;
      }

		@Override
      public void destory() {
      }
	}
}
