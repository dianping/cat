package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.heartbeat.entity.DisplayPolicy;
import com.dianping.cat.home.heartbeat.entity.Group;
import com.dianping.cat.home.heartbeat.entity.Metric;
import com.dianping.cat.home.heartbeat.transform.DefaultSaxParser;

public class DisplayPolicyManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private static final int K = 1024;

	private int m_configId;

	private DisplayPolicy m_config;

	private static final String CONFIG_NAME = "displayPolicy";

	public DisplayPolicy getDisplayPolicy() {
		return m_config;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new DisplayPolicy();
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	public boolean isDelta(String metricName) {
		for (Group group : m_config.getGroups().values()) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				return metric.isDelta();
			}
		}
		return false;
	}

	public List<String> queryAlertMetrics() {
		List<String> metrics = new ArrayList<String>();

		for (Group group : m_config.getGroups().values()) {
			for (Metric metric : group.getMetrics().values()) {
				if (metric.isAlert()) {
					metrics.add(metric.getId());
				}
			}
		}
		return metrics;
	}

    public String queryUnitName(String metricName, String defaultUnitName) {
        for (Group group : m_config.getGroups().values()) {
            if (group.findMetric(metricName) != null) {
                Metric metric = group.findMetric(metricName);
                if (metric != null) {
                    return metric.getUnit();
                }
            }
        }
        return defaultUnitName;
    }

    public int queryUnit(String metricName) {
        String unitName = queryUnitName(metricName, null);
        if (null != unitName) {
            if ("K".equals(unitName) || "KB".equals(unitName)) {
                return K;
            } else if ("M".equals(unitName) || "MB".equals(unitName)) {
                return K * K;
            }
        }
        return 1;
    }

	public List<String> sortGroupNames(List<String> originGroupNames) {
		List<Group> groups = new ArrayList<Group>();

		for (Entry<String, Group> entry : m_config.getGroups().entrySet()) {
			if (originGroupNames.contains(entry.getKey())) {
				groups.add(entry.getValue());
			}
		}
		Collections.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group g1, Group g2) {
				return g1.getOrder() - g2.getOrder();
			}
		});

		List<String> result = new ArrayList<String>();

		for (Group group : groups) {
			result.add(group.getId());
		}
		for (String originGroupName : originGroupNames) {
			if (!result.contains(originGroupName)) {
				result.add(originGroupName);
			}
		}
		return result;
	}

	public List<String> sortGroupNames(Set<String> originGroupNameSet) {
		return sortGroupNames(new ArrayList<String>(originGroupNameSet));
	}

	public List<String> sortMetricNames(String groupName, List<String> originMetricNames) {
		Group group = m_config.findGroup(groupName);
		List<String> result = new ArrayList<String>();

		if (group != null) {
			List<Metric> list = new ArrayList<Metric>();

			for (Entry<String, Metric> entry : group.getMetrics().entrySet()) {
				if (originMetricNames.contains(entry.getKey())) {
					list.add(entry.getValue());
				}
			}
			Collections.sort(list, new Comparator<Metric>() {
				@Override
				public int compare(Metric m1, Metric m2) {
					return m1.getOrder() - m2.getOrder();
				}
			});
			for (Metric metric : list) {
				result.add(metric.getId());
			}
		}

		for (String originMetricName : originMetricNames) {
			if (!result.contains(originMetricName)) {
				result.add(originMetricName);
			}
		}
		return result;
	}

	public List<String> sortMetricNames(String groupName, Set<String> originMetricNames) {
		return sortMetricNames(groupName, new ArrayList<String>(originMetricNames));
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
