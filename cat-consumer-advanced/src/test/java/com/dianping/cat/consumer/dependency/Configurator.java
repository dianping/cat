package com.dianping.cat.consumer.dependency;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.Constants;
import com.dianping.cat.DomainManager;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;

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
		all.add(C(DomainManager.class, ExtendedDomainManager.class));

		return all;
	}

	public static class ExtendedDependencyDelegate extends DependencyDelegate {
	}

	public static class MockDependencyReportManager extends MockReportManager<DependencyReport> {
		private DependencyReport m_report;

		@Inject
		private ReportDelegate<DependencyReport> m_delegate;

		@Override
		public DependencyReport getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
			if (m_report == null) {
				m_report = (DependencyReport) m_delegate.makeReport(domain, startTime, Constants.HOUR);
			}

			return m_report;
		}
	}

	public static class ExtendedDomainManager extends DomainManager {
		
		@Override
		public void initialize() throws InitializationException{
		}
		
		@Override
		public String queryDomainByIp(String ip) {
			return "Cat-CatTest";
		}
	}
}
