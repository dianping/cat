/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
