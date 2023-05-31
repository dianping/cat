package com.dianping.cat.config.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.app.speed.entity.AppSpeedConfig;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.configuration.app.speed.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named(type = AppSpeedConfigManager.class)
public class AppSpeedConfigManager implements Initializable {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private volatile Map<String, Speed> m_speeds = new ConcurrentHashMap<String, Speed>();

	private volatile AppSpeedConfig m_config;

	private int m_configId;

	private long m_modifyTime;

	private static final String CONFIG_NAME = "app-speed-config";

	@Override
	public void initialize() {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
			updateData();
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
			m_config = new AppSpeedConfig();
		}
		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}

			@Override
			public String getName() {
				return CONFIG_NAME;
			}
		});
	}

	public int generateId() {
		List<Integer> ids = new ArrayList<Integer>();

		for (Speed s : m_config.getSpeeds().values()) {
			ids.add(s.getId());
		}
		int max = 0;

		if (!ids.isEmpty()) {
			Collections.sort(ids);
			max = ids.get(ids.size() - 1);
		}

		if (ids.size() < max) {
			for (int i = 1; i <= max; i++) {
				if (!ids.contains(i)) {
					return i;
				}
			}
		}
		return max + 1;
	}

	public Map<String, List<Speed>> getPageStepInfo() {
		Map<String, List<Speed>> page2Steps = new HashMap<String, List<Speed>>();

		for (Speed speed : m_config.getSpeeds().values()) {
			String page = speed.getPage();
			if (StringUtils.isEmpty(page)) {
				page = "default";
			}
			List<Speed> steps = page2Steps.get(page);
			if (steps == null) {
				steps = new ArrayList<Speed>();
				page2Steps.put(page, steps);
			}
			steps.add(speed);
		}
		for (Entry<String, List<Speed>> entry : page2Steps.entrySet()) {
			List<Speed> speeds = entry.getValue();
			Collections.sort(speeds, new Comparator<Speed>() {

				@Override
				public int compare(Speed o1, Speed o2) {
					return o1.getStep() - o2.getStep();
				}
			});
		}
		return page2Steps;
	}

	public AppSpeedConfig getConfig() {
		return m_config;
	}

	public boolean deleteSpeed(int id) {
		m_config.removeSpeed(id);
		return storeConfig();
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

	public boolean updateConfig(Speed speed) {
		m_config.addSpeed(speed);
		return storeConfig();
	}

	private void refreshConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				AppSpeedConfig appConfig = DefaultSaxParser.parse(content);

				m_config = appConfig;
				m_modifyTime = modifyTime;
				updateData();
			}
		}
	}

	public int querSpeedThreshold(String page, String step) {
		int value = -1;
		Speed speed = m_speeds.get(page + "-" + step);

		if (speed != null) {
			value = speed.getThreshold();
		}
		return value;
	}

	public int querySpeedId(String page, String step) {
		int value = -1;
		Speed speed = m_speeds.get(page + "-" + step);

		if (speed != null) {
			value = speed.getId();
		}
		return value;
	}

	public Set<Integer> querySpeedIds() {
		return m_config.getSpeeds().keySet();
	}

	private void updateData() {
		Map<Integer, Speed> speeds = m_config.getSpeeds();
		Map<String, Speed> tmp = new ConcurrentHashMap<String, Speed>();

		for (Entry<Integer, Speed> entry : speeds.entrySet()) {
			Speed s = entry.getValue();

			tmp.put(s.getPage() + "-" + s.getStep(), s);
		}
		m_speeds = tmp;
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_config.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);

			updateData();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}
}
