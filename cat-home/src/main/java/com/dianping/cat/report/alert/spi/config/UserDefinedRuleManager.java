package com.dianping.cat.report.alert.spi.config;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.alarm.UserDefineRule;
import com.dianping.cat.alarm.UserDefineRuleDao;
import com.dianping.cat.alarm.UserDefineRuleEntity;

@Named
public class UserDefinedRuleManager {

	@Inject
	private UserDefineRuleDao m_dao;

	public String addUserDefineText(String userDefinedText) throws DalException {
		UserDefineRule item = m_dao.findMaxId(UserDefineRuleEntity.READSET_MAXID);
		int id = 1;
		if (item != null) {
			id = item.getMaxId() + 1;
		}

		UserDefineRule userDefineRule = m_dao.createLocal();

		userDefineRule.setContent(userDefinedText);
		userDefineRule.setId(id);
		m_dao.insert(userDefineRule);
		return Integer.toString(id);
	}

	public String getUserDefineText(String idStr) throws DalException {
		int id = Integer.parseInt(idStr);

		UserDefineRule item = m_dao.findByPK(id, UserDefineRuleEntity.READSET_FULL);
		return item.getContent();
	}

	public void removeById(String id) throws DalException {
		UserDefineRule item = m_dao.createLocal();

		item.setId(Integer.parseInt(id));
		m_dao.deleteByPK(item);
	}

}
