package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.advanced.metric.config.entity.Tag;
import com.dianping.cat.consumer.metric.MetricConfigManager;

public class TagManager {

	@Inject
	private MetricConfigManager m_metricConfigManager;

	public Set<String> queryTags() {
		Set<String> tags = new HashSet<String>();

		try {
			Collection<MetricItemConfig> configs = m_metricConfigManager.getMetricConfig().getMetricItemConfigs().values();

			for (MetricItemConfig metricItemConfig : configs) {
				for (Tag tag : metricItemConfig.getTags()) {
					String tagName = tag.getName();
					if (!StringUtil.isEmpty(tagName)) {
						tags.add(tagName);
					}
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
			Collection<MetricItemConfig> configs = m_metricConfigManager.getMetricConfig().getMetricItemConfigs().values();

			for (MetricItemConfig metricItemConfig : configs) {
				for (Tag itemTag : metricItemConfig.getTags()) {
					String tagName = itemTag.getName();

					if (tag.equals(tagName)) {
						metricItemConfigs.add(metricItemConfig);
						break;
					}
				}
			}
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return metricItemConfigs;
	}

}
