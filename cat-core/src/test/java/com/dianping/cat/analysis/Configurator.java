package com.dianping.cat.analysis;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.message.spi.MessageTree;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return DefaultMessageAnalyzerManagerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageAnalyzer.class, "mock1", MockAnalyzer1.class).is(PER_LOOKUP));
		all.add(C(MessageAnalyzer.class, "mock2", MockAnalyzer2.class).is(PER_LOOKUP));
		all.add(C(MessageAnalyzer.class, "state", MockAnalyzer3.class).is(PER_LOOKUP));

		return all;
	}

	public static class MockAnalyzer1 extends AbstractMessageAnalyzer<Object> {

		public int m_count = 1;

		@Override
		public void doCheckpoint(boolean atEnd) {
		}

		@Override
		public Object getReport(String domain) {
			return null;
		}

		@Override
		protected void process(MessageTree tree) {
			m_count++;
			if (m_count % 50 == 0) {
				throw new RuntimeException();
			}
		}

		@Override
      protected void loadReports() {
      }
	}

	public static class MockAnalyzer2 extends AbstractMessageAnalyzer<Object> {

		public int m_count = 2;

		@Override
		public void doCheckpoint(boolean atEnd) {
		}

		@Override
		public Object getReport(String domain) {
			return null;
		}

		@Override
		protected void process(MessageTree tree) {
			m_count++;
			if (m_count % 50 == 0) {
				throw new RuntimeException();
			}
		}

		@Override
      protected void loadReports() {
      }
	}

	public static class MockAnalyzer3 extends AbstractMessageAnalyzer<Object> {

		public int m_count = 3;

		@Override
		public void doCheckpoint(boolean atEnd) {
		}

		@Override
		public Object getReport(String domain) {
			return null;
		}

		@Override
		protected void process(MessageTree tree) {
			m_count++;
			if (m_count % 50 == 0) {
				throw new RuntimeException();
			}
		}

		@Override
      protected void loadReports() {
      }
	}
	
}
