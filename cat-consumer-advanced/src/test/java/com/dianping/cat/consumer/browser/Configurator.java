package com.dianping.cat.consumer.browser;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return BrowserAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = BrowserAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockBrowserReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedBrowserDelegate.class));
		return all;
	}

	public static class ExtendedBrowserDelegate extends BrowserDelegate {
	}

	public static class MockBrowserReportManager extends MockReportManager<BrowserReport> {
		private BrowserReport m_report;

		@Inject
		private ReportDelegate<BrowserReport> m_delegate;

		@Override
		public BrowserReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (BrowserReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}
	}
}
