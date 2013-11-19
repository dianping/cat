package com.dianping.cat.consumer.advanced;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.abtest.spi.internal.ABTestCodec;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.storage.BucketManager;
import com.dianping.cat.task.TaskManager;

public class Configurator extends AbstractResourceConfigurator {

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new Configurator());
	}

	protected Class<?> getTestClass() {
		return MetricAnalyzerTest.class;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		final String ID = MetricAnalyzer.ID;

		all.add(C(MetricConfigManager.class, ExtendedMetricConfigManager.class));
		all.add(C(ProductLineConfigManager.class));
		all.add(C(TaskManager.class, MockTaskManager.class));
		all.add(C(MessageAnalyzer.class, ID, MetricAnalyzer.class) //
		      .req(BucketManager.class, MetricConfigManager.class)//
		      .req(ProductLineConfigManager.class, ABTestCodec.class, TaskManager.class));

		return all;
	}

	public static class ExtendedMetricConfigManager extends MetricConfigManager {

		private MetricItemConfig m_config = new MetricItemConfig();
		
		@Override
		public MetricItemConfig queryMetricItemConfig(String id) {
			return m_config;
		}
		
		@Override
		public boolean insertMetricItemConfig(MetricItemConfig config) {
			return true;
		}
	}

	public static class MockTaskManager extends TaskManager {
		private Map<Integer, Set<String>> m_results = new HashMap<Integer, Set<String>>();

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		@Override
		protected void createTask(Date period, String ip, String domain, int reportType) throws DalException {
			Set<String> lists = m_results.get(reportType);

			if (lists == null) {
				lists = new HashSet<String>();
				m_results.put(reportType, lists);
			}

			lists.add(sdf.format(period));
		}

		public Map<Integer, Set<String>> getResults() {
			return m_results;
		}

	}
}
