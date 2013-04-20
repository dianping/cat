package com.dianping.cat.consumer.demo;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.consumer.MessageAnalyzerFactory;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.demo.OneAnalyzerTwoDurationTest.MockAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;

public class OneAnalyzerTwoDurationTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new OneAnalyzerTwoDurationTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageConsumer.class, "mock", RealtimeConsumer.class) //
		      .config(E("analyzers").value("mock") //
		      ).req(MessageAnalyzerFactory.class)//
		);

		all.add(C(MessageAnalyzer.class, "mock", MockAnalyzer.class) //
		      .is(PER_LOOKUP));

		all.add(C(MessageAnalyzerFactory.class, OneAnalyzerMockFactory.class));

		return all;
	}

	@Override
	protected Class<?> getTestClass() {
		return OneAnalyzerTwoDurationTest.class;
	}
}
