package com.dianping.cat.system.page.config;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.ConfigModification;
import com.dianping.cat.home.dal.report.ConfigModificationDao;
import com.dianping.cat.report.page.JsonBuilder;

public class ConfigModificationService {

	@Inject
	private ConfigModificationDao m_configModificationDao;

	public void store(String userName, String accountName, Payload payload) {
		ConfigModification modification = m_configModificationDao.createLocal();

		modification.setUserName(userName);
		modification.setAccountName(accountName);
		modification.setActionName(payload.getAction().getName());
		modification.setDate(new Date());
		modification.setArgument(new JsonBuilder().toJson(payload));

		try {
			m_configModificationDao.insert(modification);
		} catch (Exception ex) {
			Cat.logError(ex);
		}
	}
}
