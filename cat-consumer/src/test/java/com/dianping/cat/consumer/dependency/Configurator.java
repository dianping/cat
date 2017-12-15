package com.dianping.cat.consumer.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.service.HostinfoService;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return DependencyAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = DependencyAnalyzer.ID;

		all.add(C(ReportManager.class, ID, MockDependencyReportManager.class)//
		      .req(ReportDelegate.class, ID));
		all.add(C(ReportDelegate.class, ID, ExtendedDependencyDelegate.class));
		all.add(C(HostinfoService.class, ExtendedHostinfoService.class));

		return all;
	}

	public static class ExtendedDependencyDelegate extends DependencyDelegate {
	}

	public static class MockDependencyReportManager extends MockReportManager<DependencyReport> {
		
		private Map<String,DependencyReport> m_reports = new HashMap<String, DependencyReport>();

		@Inject
		private ReportDelegate<DependencyReport> m_delegate;

		@Override
		public DependencyReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			DependencyReport report = m_reports.get(domain);
			
			if (report == null) {
				report = (DependencyReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
				
				m_reports.put(domain, report);
			}

			return report;
		}

		@Override
      public void destory() {
      }
	}

	public static class ExtendedHostinfoService extends HostinfoService {

		@Override
		public void initialize() throws InitializationException {
		}

		@Override
		public String queryDomainByIp(String ip) {
			return "Cat-CatTest";
		}
	}
}
