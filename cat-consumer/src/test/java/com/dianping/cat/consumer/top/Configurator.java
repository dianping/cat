package com.dianping.cat.consumer.top;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return TopAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = TopAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockTopReportManager.class)//
		      .req(ReportDelegate.class, ID).is(PER_LOOKUP));
		all.add(C(ReportDelegate.class, ID, ExtendedTopDelegate.class));

		return all;
	}

	public static class ExtendedTopDelegate extends TopDelegate {
	}

	public static class MockTopReportManager extends MockReportManager<TopReport> {
		private TopReport m_report;

		@Inject
		private ReportDelegate<TopReport> m_delegate;

		@Override
		public TopReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (TopReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
      public void destory() {
      }
	}
}
