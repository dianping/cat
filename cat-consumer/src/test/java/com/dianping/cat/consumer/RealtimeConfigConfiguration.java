package com.dianping.cat.consumer;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.Configurator;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzerTest;

public class RealtimeConfigConfiguration extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return TransactionAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		return all;
	}

	public static class MockMessageAnalyzerManager implements MessageAnalyzerManager {

		@Override
		public List<String> getAnalyzerNames() {
			List<String> list = new ArrayList<String>();

			list.add(TransactionAnalyzer.ID);
			list.add(EventAnalyzer.ID);
			list.add(TopAnalyzer.ID);
			return list;
		}

		@Override
		public List<MessageAnalyzer> getAnalyzer(String name, long startTime) {
			return null;
		}
	}
}
