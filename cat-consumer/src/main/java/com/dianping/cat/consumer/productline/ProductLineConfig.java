package com.dianping.cat.consumer.productline;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.company.model.entity.Company;

public enum ProductLineConfig {

	METRIC("metric", "业务监控", ""),

	USER("user", "外部监控", Constants.BROKER_SERVICE),

	APPLICATION("application", "应用监控", ""),

	NETWORK("network", "网络监控", "switch-|f5-"),

	SYSTEM("system", "系统监控", "system-"),

	DATABASE("database", "数据库监控", "db-"),

	CDN("cdn", "CDN监控", "cdn");

	private String m_configName;

	private String m_title;

	private List<String> m_prefixs = new ArrayList<String>();

	private int m_configId;

	private Company m_company;

	private long m_modifyTime;

	private ProductLineConfig(String config, String title, String prefix) {
		m_configName = config;
		m_title = title;

		if (StringUtils.isNotEmpty(prefix)) {
			if (prefix.contains("|")) {
				String[] s = prefix.split("\\|");

				for (int i = 0; i < s.length; i++) {
					m_prefixs.add(s[i].trim());
				}
			} else {
				m_prefixs.add(prefix);
			}
		}
	}

	public Company getCompany() {
		synchronized (this) {
			return m_company;
		}
	}

	public int getConfigId() {
		return m_configId;
	}

	public String getConfigName() {
		return m_configName;
	}

	public long getModifyTime() {
		return m_modifyTime;
	}

	public List<String> getPrefix() {
		return m_prefixs;
	}

	public String getTitle() {
		return m_title;
	}

	public boolean isTypeOf(String productline) {
		if (StringUtils.isNotEmpty(productline)) {
			if (m_prefixs.isEmpty()) {
				return true;
			} else if (m_prefixs.contains(Constants.BROKER_SERVICE) && Constants.BROKER_SERVICE.equals(productline)) {
				return true;
			} else if (m_prefixs.contains("cdn") && "cdn".equals(productline)) {
				return true;
			} else {
				for (String prefix : m_prefixs) {
					if (productline.startsWith(prefix)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean needCheckDuplicate() {
		return ProductLineConfig.METRIC.equals(this) || ProductLineConfig.APPLICATION.equals(this);
	}

	public void setCompany(Company company) {
		m_company = company;
	}

	public void setConfigId(int configId) {
		m_configId = configId;
	}

	public void setModifyTime(long modifyTime) {
		m_modifyTime = modifyTime;
	}

}
