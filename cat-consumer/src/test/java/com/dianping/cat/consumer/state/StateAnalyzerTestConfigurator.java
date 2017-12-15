package com.dianping.cat.consumer.state;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class StateAnalyzerTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new StateAnalyzerTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(MockStateReportManager.class));
		all.add(A(ExtendedStateDelegate.class));
		all.add(A(StateAnalyzer.class));

		return all;
	}

	protected Class<?> getTestClass() {
		return StateAnalyzerTest.class;
	}

	@Named(type = ReportDelegate.class, value = StateAnalyzer.ID)
	public static class ExtendedStateDelegate extends StateDelegate {
	}

	@Named(type = ReportManager.class, value = StateAnalyzer.ID)
	public static class MockStateReportManager extends MockReportManager<StateReport> {
		@Inject(StateAnalyzer.ID)
		private ReportDelegate<StateReport> m_delegate;

		private StateReport m_report;

		@Override
		public void destory() {
		}

		@Override
		public StateReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (StateReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}
	}
}
