package com.dianping.cat.consumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.OneAnalyzerTwoDurationTest.MockAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class OneAnalyzerTwoDurationTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new OneAnalyzerTwoDurationTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageConsumer.class, "mock", RealtimeConsumer.class) //
		      .config(E("analyzers").value("mock") //
		      ).req(AnalyzerFactory.class)//
		);

		all.add(C(MessageAnalyzer.class, "mock", MockAnalyzer.class) //
		      .is(PER_LOOKUP));

		all.add(C(AnalyzerFactory.class, OneAnalyzerMockFactory.class));

		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + OneAnalyzerTwoDurationTest.class.getName().replace('.', '/') + ".xml");
	}
}
