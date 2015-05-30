package com.dianping.cat.transaction;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.transaction.analyzer.TransactionAnalyzer;
import com.dianping.cat.transaction.analyzer.TransactionDelegate;
import com.dianping.cat.transaction.model.entity.TransactionReport;

public class TransactionConfigurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new TransactionConfigurator());
	}

	protected Class<?> getTestClass() {
		return TransactionPerformanceTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = TransactionAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockTransactionReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedTransactionDelegate.class));

		return all;
	}

	public static class ExtendedTransactionDelegate extends TransactionDelegate {
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
      public void destory() {
      }
	}
	
	public static class MockMessageAnalyzerManager implements MessageAnalyzerManager {

		@Override
		public List<String> getAnalyzerNames() {
			List<String> list = new ArrayList<String>();

			list.add(TransactionAnalyzer.ID);
			return list;
		}

		@Override
		public List<MessageAnalyzer> getAnalyzer(String name, long startTime) {
			return null;
		}
	}
}
