package com.dianping.cat.transaction;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.transaction.analyzer.TransactionAnalyzer;

public class TransactionAnalyzerConfiguration extends AbstractResourceConfigurator {

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
			return list;
		}

		@Override
		public List<MessageAnalyzer> getAnalyzer(String name, long startTime) {
			return null;
		}
	}
}
