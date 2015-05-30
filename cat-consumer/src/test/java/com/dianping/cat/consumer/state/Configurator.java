package com.dianping.cat.consumer.state;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.statistic.ServerStatisticManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return StateAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = StateAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockStateReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedStateDelegate.class));
		all.add(C(MessageAnalyzer.class, ID, StateAnalyzer.class).req(ReportManager.class, ID)
		      .req(ServerConfigManager.class, ServerStatisticManager.class).config(E("m_ip").value("192.168.1.1")));

		return all;
	}

	public static class ExtendedStateDelegate extends StateDelegate {
	}

	public static class MockStateReportManager extends MockReportManager<StateReport> {
		private StateReport m_report;

		@Inject
		private ReportDelegate<StateReport> m_delegate;

		@Override
		public StateReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (StateReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		@Override
      public void destory() {
      }
	}
	
}
