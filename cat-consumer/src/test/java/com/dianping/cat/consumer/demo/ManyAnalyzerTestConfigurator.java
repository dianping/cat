package com.dianping.cat.consumer.demo;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.MessageAnalyzerFactory;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer1;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer2;
import com.dianping.cat.consumer.demo.ManyAnalyzerTest.MockAnalyzer3;
import com.dianping.cat.message.spi.MessageConsumer;

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
	protected Class<?> getTestClass() {
		return ManyAnalyzerTest.class;
	}
}
