package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.display.policy.entity.DisplayPolicy;
import com.dianping.cat.home.display.policy.entity.Group;
import com.dianping.cat.home.display.policy.entity.Metric;
import com.dianping.cat.home.display.policy.transform.DefaultSaxParser;

public class DisplayPolicyManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_getter;

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
				String content = m_getter.getConfigContent(CONFIG_NAME);
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

	public boolean isDelta(String groupName, String metricName) {
		Group group = m_config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				return metric.isIsDelta();
			}
		}
		return false;
	}

	public List<String> queryMetrics() {
		List<String> metrics = new ArrayList<String>();

		for (Group group : m_config.getGroups().values()) {
			for (Metric metric : group.getMetrics().values()) {
				metrics.add(metric.getId());
			}
		}
		return metrics;
	}

	public List<String> queryOrderedGroupNames() {
		List<Group> groups = new ArrayList<Group>();
		List<String> names = new ArrayList<String>();

		for (Group group : m_config.getGroups().values()) {
			groups.add(group);
		}
		Collections.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group g1, Group g2) {
				return g1.getOrder() - g2.getOrder();
			}
		});
		for (Group group : groups) {
			names.add(group.getId());
		}
		return names;
	}

	public List<String> queryOrderedMetricNames(String groupName) {
		Group group = m_config.findGroup(groupName);
		List<Metric> list = new ArrayList<Metric>();
		List<String> metricNames = new ArrayList<String>();

		if (group != null) {
			for (Metric metric : group.getMetrics().values()) {
				list.add(metric);
			}
			Collections.sort(list, new Comparator<Metric>() {
				@Override
				public int compare(Metric m1, Metric m2) {
					return m1.getOrder() - m2.getOrder();
				}
			});
			for (Metric metric : list) {
				metricNames.add(metric.getId());
			}
		}
		return metricNames;
	}

	public int queryUnit(String groupName, String metricName) {
		Group group = m_config.findGroup(groupName);

		if (group != null) {
			Metric metric = group.findMetric(metricName);

			if (metric != null) {
				String metricUnit = metric.getUnit();

				if ("K".equals(metricUnit)) {
					return K;
				} else if ("M".equals(metricUnit)) {
					return K * K;
				}
			}
		}
		return 1;
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
