/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-11
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.server;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.cobar.CobarPrivileges;
import com.alibaba.cobar.net.handler.FrontendPrivileges;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class BeeFrontendPrivileges implements FrontendPrivileges {

	private CobarPrivileges m_cobarPrivilegs;

	private Set<String> m_database;

	public BeeFrontendPrivileges(CobarPrivileges cobarPrivileges, Set<String> list) {
		this.m_cobarPrivilegs = cobarPrivileges;
		this.m_database = list;
	}

	@Override
	public boolean schemaExists(String schema) {
		return this.m_database.contains(schema);
	}

	@Override
	public boolean userExists(String user, String host) {
		return m_cobarPrivilegs.userExists(user, host);
	}

	@Override
	public String getPassword(String user) {
		return m_cobarPrivilegs.getPassword(user);
	}

	@Override
	public Set<String> getUserSchemas(String user) {
		if (getPassword(user) != null) {
			return m_database;
		} else {
			return new HashSet<String>(0);
		}
	}

}
