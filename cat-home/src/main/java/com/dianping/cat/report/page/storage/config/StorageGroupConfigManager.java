package com.dianping.cat.report.page.storage.config;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.storage.entity.Link;
import com.dianping.cat.home.storage.entity.Machine;
import com.dianping.cat.home.storage.entity.Storage;
import com.dianping.cat.home.storage.entity.StorageGroup;
import com.dianping.cat.home.storage.entity.StorageGroupConfig;
import com.dianping.cat.home.storage.transform.DefaultSaxParser;
import com.dianping.cat.report.page.storage.StorageConstants;

public class StorageGroupConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private StorageGroupConfig m_config;

	private static final String CONFIG_NAME = "storageGroup";

	public static final String DEFAULT = "Default";

	private String m_sqlLinkFormat;

	private String m_cacheLinkFormat;

	public StorageGroupConfig getConfig() {
		return m_config;
	}

	public String queryLinkFormat(String type) {
		if (StorageConstants.SQL_TYPE.equals(type)) {
			return m_sqlLinkFormat;
		} else if (StorageConstants.CACHE_TYPE.equals(type)) {
			return m_cacheLinkFormat;
		} else {
			return null;
		}
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
		refreshData();
	}

	public boolean isSQLAlertMachine(String id, String ip, String type) {
		boolean result = true;
		StorageGroup group = m_config.getStorageGroups().get(type);

		if (group != null) {
			Storage storage = group.getStorages().get(id);

			if (storage != null) {
				Machine machine = storage.getMachines().get(ip);

				if (machine != null) {
					return machine.getAlert();
				}
			}
		}
		return result;
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

	private void refreshData() {
		m_sqlLinkFormat = refreshLinkFormat(StorageConstants.SQL_TYPE);
		m_cacheLinkFormat = refreshLinkFormat(StorageConstants.CACHE_TYPE);
	}

	private String refreshLinkFormat(String type) {
		StorageGroup group = queryStorageGroup(type);
		Link link = group.getLink();

		if (link != null) {
			String url = link.getUrl();
			List<String> pars = link.getPars();

			return url + "?" + StringUtils.join(pars, "&");
		} else {
			return null;
		}
	}

	public String buildUrl(String format, String id, String ip) {
		try {
			return format.replace(StorageConstants.ID_FORMAT, URLEncoder.encode(id, "utf-8")).replace(
			      StorageConstants.IP_FORMAT, URLEncoder.encode(ip, "utf-8"));
		} catch (Exception e) {
			Cat.logError("can't encode [id: " + id + "] [ip: " + ip + "]", e);
			return null;
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
		refreshData();
		return true;
	}

	public Map<String, Department> queryStorageDepartments(List<String> ids, String type) {
		Map<String, Department> departments = new LinkedHashMap<String, Department>();

		for (String id : ids) {
			Storage storage = queryStorageGroup(type).getStorages().get(id);
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
