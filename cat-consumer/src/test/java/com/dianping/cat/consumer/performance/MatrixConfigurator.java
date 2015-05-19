package com.dianping.cat.consumer.performance;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixDelegate;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;

public class MatrixConfigurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new MatrixConfigurator());
	}

	protected Class<?> getTestClass() {
		return MatrixPerformanceTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = MatrixAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockMatrixReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedMatrixDelegate.class));

		return all;
	}

	public static class ExtendedMatrixDelegate extends MatrixDelegate {
	}

	public static class MockMatrixReportManager extends MockReportManager<MatrixReport> {
		private MatrixReport m_report;

		@Inject
		private ReportDelegate<MatrixReport> m_delegate;

		@Override
		public MatrixReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (MatrixReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}
			
			return m_report;
		}

		public void setReport(MatrixReport report) {
      	m_report = report;
      }

		@Override
      public void destory() {
      }
	}
}
