package com.dianping.cat.consumer.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.impl.ManyAnalyzerTest.MockAnalyzer1;
import com.dianping.cat.consumer.impl.ManyAnalyzerTest.MockAnalyzer2;
import com.dianping.cat.consumer.impl.ManyAnalyzerTest.MockAnalyzer3;
import com.dianping.cat.message.consumer.impl.AnalyzerFactory;
import com.dianping.cat.message.consumer.impl.RealtimeConsumer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ManyAnalyzerTestConfigurator extends
		AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ManyAnalyzerTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageConsumer.class, "mockManyAnalyzers", RealtimeConsumer.class) //
				.req(AnalyzerFactory.class)//
				.config(E("consumerId").value("analyzers") //
						, E("analyzerNames").value("mock1,mock2,mock3") //
				));

		all.add(C(MessageAnalyzer.class, "mock1", MockAnalyzer1.class) //
				.is(PER_LOOKUP));
		all.add(C(MessageAnalyzer.class, "mock2", MockAnalyzer2.class) //
				.is(PER_LOOKUP));
		all.add(C(MessageAnalyzer.class, "mock3", MockAnalyzer3.class) //
				.is(PER_LOOKUP));

		all.add(C(AnalyzerFactory.class,ManyAnalyerMockFactory.class));
		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/"
				+ ManyAnalyzerTest.class.getName().replace('.', '/')
				+ ".xml");
	}
}
