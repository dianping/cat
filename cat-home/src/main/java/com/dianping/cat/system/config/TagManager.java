package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.MetricConfigManager;

public class TagManager {

	@Inject
	private MetricConfigManager m_metricConfigManager;

	public Set<String> queryTags() {
		Set<String> tags = new HashSet<String>();

		try {
			for (MetricItemConfig metricItemConfig : m_metricConfigManager.getMetricConfig().getMetricItemConfigs()
			      .values()) {
				String tag = metricItemConfig.getTag();

				if (!StringUtil.isEmpty(tag)) {
					tags.add(tag);
				}
			}
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return tags;
	}

	public List<MetricItemConfig> queryMetricItemConfigs(String tag) {
		List<MetricItemConfig> metricItemConfigs = new ArrayList<MetricItemConfig>();

		try {
			for (MetricItemConfig metricItemConfig : m_metricConfigManager.getMetricConfig().getMetricItemConfigs()
			      .values()) {
				String itemTag = metricItemConfig.getTag();

				if (tag.equals(itemTag)) {
					metricItemConfigs.add(metricItemConfig);
				}
			}
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return metricItemConfigs;
	}

}
