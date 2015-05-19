package com.dianping.cat.consumer.heartbeat;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return HeartbeatAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = HeartbeatAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockHeartbeatReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedHeartbeatDelegate.class));

		return all;
	}

	public static class ExtendedHeartbeatDelegate extends HeartbeatDelegate {
	}
	
	public static class MockHeartbeatReportManager extends MockReportManager<HeartbeatReport> {
		private HeartbeatReport m_report;

		@Inject
		private ReportDelegate<HeartbeatReport> m_delegate;

		@Override
		public HeartbeatReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (HeartbeatReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
      public void destory() {
      }
	}
	
}
