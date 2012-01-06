package com.dianping.cat.message.consumer.model.failure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class FailureAnalyzerTestConfigurator extends
		AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new FailureAnalyzerTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		/*String handlers = "failure,long-url";
		
		all.add(C(FailureReportAnalyzerConfig.class, "failure-analyzer-config",//
				FailureReportAnalyzerConfig.class)//
				.config(E("handlers").value(handlers)//
						, E("machines").value("192.168.1.1,192.168.1.2,192.168.1.3")));
		*/
		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/"
				+ FailureAnalyzerTest.class.getName().replace('.', '/')
				+ ".xml");
	}
}
