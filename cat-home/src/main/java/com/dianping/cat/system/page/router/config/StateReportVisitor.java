package com.dianping.cat.system.page.router.config;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.helper.SortHelper;

public class StateReportVisitor extends BaseVisitor {

	private RouterConfigManager m_routerConfigManager;

	private Map<String, Map<String, Long>> m_statistics = new HashMap<String, Map<String, Long>>();

	private Comparator<Entry<String, Long>> m_comparator = new Comparator<Map.Entry<String, Long>>() {

		@Override
		public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			long value = o2.getValue() - o1.getValue();

			if (value > 0) {
				return 1;
			} else if (value < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	public StateReportVisitor(RouterConfigManager routerConfigManager) {
		m_routerConfigManager = routerConfigManager;
	}

	private void buildStatistics(ProcessDomain processDomain, Map<String, Double> weights) {
		long total = processDomain.getTotal();

		for (Entry<String, Double> entry : weights.entrySet()) {
			String group = entry.getKey();
			Map<String, Long> datas = m_statistics.get(group);

			if (datas == null) {
				datas = new HashMap<String, Long>();

				m_statistics.put(group, datas);
			}

			String domain = processDomain.getName();
			Long value = datas.get(domain);
			long sumValue = (long) (total * entry.getValue());

			if (value == null) {
				datas.put(domain, sumValue);
			} else {
				datas.put(domain, value + sumValue);
			}
		}
	}

	private Map<String, Double> buildWeights(ProcessDomain processDomain) {
		Map<String, Double> results = new HashMap<String, Double>();
		Map<String, Integer> weights = new HashMap<String, Integer>();
		int size = processDomain.getIps().size();

		for (String ip : processDomain.getIps()) {
			String group = m_routerConfigManager.queryServerGroupByIp(ip);
			Integer value = weights.get(group);

			if (value == null) {
				weights.put(group, 1);
			} else {
				weights.put(group, value + 1);
			}
		}

		for (Entry<String, Integer> entry : weights.entrySet()) {
			results.put(entry.getKey(), (double) entry.getValue() / size);
		}
		return results;
	}

	public Map<String, Map<String, Long>> getStatistics() {
		Map<String, Map<String, Long>> datas = new HashMap<String, Map<String, Long>>();

		for (Entry<String, Map<String, Long>> entry : m_statistics.entrySet()) {
			Map<String, Long> ms = SortHelper.sortMap(entry.getValue(), m_comparator);
			datas.put(entry.getKey(), ms);
		}
		return datas;
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		Map<String, Double> weights = buildWeights(processDomain);

		buildStatistics(processDomain, weights);
	}
}
