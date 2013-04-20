package com.dianping.cat.consumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.ManyAnalyzerTest.MockAnalyzer1;
import com.dianping.cat.consumer.ManyAnalyzerTest.MockAnalyzer2;
import com.dianping.cat.consumer.ManyAnalyzerTest.MockAnalyzer3;
import com.dianping.cat.message.spi.MessageConsumer;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public class ManyAnalyzerTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ManyAnalyzerTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageConsumer.class, "mockManyAnalyzers", RealtimeConsumer.class) //
		      .req(MessageAnalyzerFactory.class)//
		      .config(E("analyzers").value("mock1,mock2,mock3") //
		      ));

		all.add(C(MessageAnalyzer.class, "mock1", MockAnalyzer1.class) //
		      .is(PER_LOOKUP));
		all.add(C(MessageAnalyzer.class, "mock2", MockAnalyzer2.class) //
		      .is(PER_LOOKUP));
		all.add(C(MessageAnalyzer.class, "mock3", MockAnalyzer3.class) //
		      .is(PER_LOOKUP));

		all.add(C(MessageAnalyzerFactory.class, ManyAnalyerMockFactory.class));
		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + ManyAnalyzerTest.class.getName().replace('.', '/') + ".xml");
	}
}
