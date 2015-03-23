package com.dianping.cat.consumer.top;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.problem.Configurator.ExtendedProblemDelegate;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.Configurator.ExtendedTransactionDelegate;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;

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

		all.add(C(ReportManager.class, "transaction", MockTransactionReportManager.class)//
		      .req(ReportDelegate.class, "transaction"));
		all.add(C(ReportDelegate.class, "transaction", ExtendedTransactionDelegate.class));

		all.add(C(ReportManager.class, "problem", MockProblemReportManager.class)//
		      .req(ReportDelegate.class, "problem"));
		all.add(C(ReportDelegate.class, "problem", ExtendedProblemDelegate.class));

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
	}

	public static class MockTransactionReportManager extends MockReportManager<TransactionReport> {
		private TransactionReport m_report;

		@Inject
		private ReportDelegate<TransactionReport> m_delegate;

		@Override
		public TransactionReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (TransactionReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}

		public void setReport(TransactionReport report) {
			m_report = report;
		}

		@Override
		public Set<String> getDomains(long startTime) {
			HashSet<String> set = new HashSet<String>();

			set.add("Cat");

			return set;
		}
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
		public Set<String> getDomains(long startTime) {
			HashSet<String> set = new HashSet<String>();

			set.add("Cat");

			return set;
		}
	}
}
