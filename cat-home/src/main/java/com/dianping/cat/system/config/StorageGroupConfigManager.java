package com.dianping.cat.system.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.storage.entity.Storage;
import com.dianping.cat.home.storage.entity.StorageGroup;
import com.dianping.cat.home.storage.entity.StorageGroupConfig;
import com.dianping.cat.home.storage.transform.DefaultSaxParser;

public class StorageGroupConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private StorageGroupConfig m_config;

	private static final String CONFIG_NAME = "storageGroup";

	public static final String DATABASE_TYPE = "database";

	public static final String CACHE_TYPE = "cache";

	public static final String DEFAULT = "Default";

	public StorageGroupConfig getConfig() {
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
			m_config = new StorageGroupConfig();
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

	public StorageGroup queryStorageGroup(String type) {
		StorageGroup group = m_config.getStorageGroups().get(type);

		if (group != null) {
			return group;
		} else {
			return new StorageGroup();
		}
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

	public Map<String, Department> queryStorageDepartments() {
		Map<String, Department> departments = new LinkedHashMap<String, Department>();

		for (Storage storage : queryStorageGroup(DATABASE_TYPE).getStorages().values()) {
			String id = storage.getId();
			String department = storage.getDepartment();
			String product = storage.getProductline();
			Department depart = departments.get(department);

			if (depart == null) {
				depart = new Department(department);

				departments.put(department, depart);
			}

			depart.findOrCreateProductline(product).addStorage(id);
		}
		return departments;
	}

	public Map<String, Department> queryStorageDepartments(List<String> ids) {
		Map<String, Department> departments = new LinkedHashMap<String, Department>();

		for (String id : ids) {
			Storage storage = queryStorageGroup(DATABASE_TYPE).getStorages().get(id);
			String department;
			String product;

			if (storage != null) {
				department = storage.getDepartment();
				product = storage.getProductline();
			} else {
				department = DEFAULT;
				product = DEFAULT;
			}
			Department depart = departments.get(department);

			if (depart == null) {
				depart = new Department(department);

				departments.put(department, depart);
			}

			depart.findOrCreateProductline(product).addStorage(id);
		}
		return departments;
	}

	public static class Department {

		private String m_id;

		private Map<String, Productline> m_productlines = new LinkedHashMap<String, Productline>();

		public Department(String id) {
			m_id = id;
		}

		public String getId() {
			return m_id;
		}

		public Map<String, Productline> getProductlines() {
			return m_productlines;
		}

		public Productline findOrCreateProductline(String productline) {
			Productline product = m_productlines.get(productline);

			if (product == null) {
				product = new Productline(productline);

				m_productlines.put(productline, product);
			}
			return product;
		}
	}

	public static class Productline {

		private String m_id;

		private List<String> m_storages = new LinkedList<String>();

		public Productline(String id) {
			m_id = id;
		}

		public String getId() {
			return m_id;
		}

		public List<String> addStorage(String storage) {
			m_storages.add(storage);
			return m_storages;
		}

		public List<String> getStorages() {
			return m_storages;
		}

	}
}
