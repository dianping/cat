package com.dianping.cat.consumer.failure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer.Handler;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class FailureAnaylzerStoreTestConfigurator extends
		AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new FailureAnaylzerStoreTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		all.add(C(FailureReportAnalyzer.class) //
				.config(E("reportPath").value("./target/report/failure/"))
				.is(PER_LOOKUP)//
				.req(Handler.class, new String[] { "failure", "long-url" },
						"m_handlers"));
		
		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/"
				+ FailureAnalyzerStoreTest.class.getName().replace('.', '/')
				+ ".xml");
	}
}
