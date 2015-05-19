package com.dianping.cat.consumer.performance;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossDelegate;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class CrossConfigurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new CrossConfigurator());
	}

	protected Class<?> getTestClass() {
		return CrossPerformanceTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = CrossAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockCrossReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedCrossDelegate.class));

		return all;
	}

	public static class ExtendedCrossDelegate extends CrossDelegate {
	}

	public static class MockCrossReportManager extends MockReportManager<CrossReport> {
		private CrossReport m_report;

		@Inject
		private ReportDelegate<CrossReport> m_delegate;

		@Override
		public CrossReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (CrossReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}
			
			return m_report;
		}

		public void setReport(CrossReport report) {
      	m_report = report;
      }

		@Override
      public void destory() {
      }
	}
}
