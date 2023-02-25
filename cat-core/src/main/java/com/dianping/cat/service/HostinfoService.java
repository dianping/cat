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
package com.dianping.cat.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.HostinfoEntity;
import com.dianping.cat.helper.TimeHelper;

@Named(type = HostinfoService.class)
public class HostinfoService implements Initializable, LogEnabled {

	public static final String UNKNOWN_PROJECT = "UnknownProject";

	protected Logger m_logger;

	@Inject
	private HostinfoDao m_hostinfoDao;

	@Inject
	private ServerConfigManager m_manager;

	private Map<String, String> m_ipDomains = new ConcurrentHashMap<String, String>();

	private Map<String, Hostinfo> m_hostinfos = new ConcurrentHashMap<String, Hostinfo>();

	public Hostinfo createLocal() {
		return m_hostinfoDao.createLocal();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public List<Hostinfo> findAll() throws DalException {
		return new ArrayList<Hostinfo>(m_hostinfos.values());
	}

	public Hostinfo findByIp(String ip) {
		Hostinfo hostinfo = m_hostinfos.get(ip);

		if (hostinfo != null) {
			return hostinfo;
		} else {
			try {
				hostinfo = m_hostinfoDao.findByIp(ip, HostinfoEntity.READSET_FULL);

				if (hostinfo != null) {
					m_hostinfos.put(ip, hostinfo);
					return hostinfo;
				} else {
					return null;
				}
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
			return null;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("cat").start(new RefreshHost());
	}

	private boolean insert(Hostinfo hostinfo) throws DalException {
		m_hostinfos.put(hostinfo.getIp(), hostinfo);

		int result = m_hostinfoDao.insert(hostinfo);
		if (result == 1) {
			return true;
		} else {
			return false;
		}
	}

	public boolean insert(String domain, String ip) {
		try {
			Hostinfo info = createLocal();

			info.setDomain(domain);
			info.setIp(ip);
			insert(info);
			m_hostinfos.put(ip, info);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	public String queryDomainByIp(String ip) {
		String project = m_ipDomains.get(ip);

		if (project == null) {
			return UNKNOWN_PROJECT;
		}
		return project;
	}

	public String queryHostnameByIp(String ip) {
		try {
			if (validateIp(ip)) {
				Hostinfo info = m_hostinfos.get(ip);
				String hostname = null;

				if (info != null) {
					hostname = info.getHostname();

					if (StringUtils.isNotEmpty(hostname)) {
						return hostname;
					}
				}
				info = findByIp(ip);

				if (info != null) {
					m_hostinfos.put(ip, info);
					hostname = info.getHostname();
				}
				return hostname;
			} else {
				return null;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}

	public List<String> queryIpsByDomain(String domain) {
		List<String> ips = new ArrayList<String>();
		if (domain == null) {
			return ips;
		}

		for (Hostinfo hostinfo : m_hostinfos.values()) {
			if (domain.equals(hostinfo.getDomain())) {
				String ip = hostinfo.getIp();

				if (ip != null && ip.length() > 0) {
					ips.add(ip);
				}
			}
		}

		return ips;
	}

	protected void refresh() {
		try {
			List<Hostinfo> hostinfos = m_hostinfoDao.findAllIp(HostinfoEntity.READSET_FULL);
			Map<String, Hostinfo> tmpHostInfos = new ConcurrentHashMap<String, Hostinfo>();
			Map<String, String> tmpIpDomains = new ConcurrentHashMap<String, String>();

			for (Hostinfo hostinfo : hostinfos) {
				tmpHostInfos.put(hostinfo.getIp(), hostinfo);
				tmpIpDomains.put(hostinfo.getIp(), hostinfo.getDomain());
			}
			m_hostinfos = tmpHostInfos;
			m_ipDomains = tmpIpDomains;
		} catch (DalException e) {
			Cat.logError("initialize HostService error", e);
		}
	}

	public boolean update(int id, String domain, String ip) {
		Hostinfo info = createLocal();

		info.setId(id);
		info.setDomain(domain);
		info.setIp(ip);
		info.setLastModifiedDate(new Date());
		updateHostinfo(info);
		m_hostinfos.put(ip, info);
		return true;
	}

	public boolean updateHostinfo(Hostinfo hostinfo) {
		m_hostinfos.put(hostinfo.getIp(), hostinfo);

		try {
			m_hostinfoDao.updateByPK(hostinfo, HostinfoEntity.UPDATESET_FULL);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	private boolean validateIp(String str) {
		Pattern pattern = Pattern
								.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

	public class RefreshHost implements Runnable {

		@Override
		public void run() {
			while (true) {
				refresh();

				try {
					Thread.sleep(TimeHelper.ONE_MINUTE);
				} catch (InterruptedException e) {
					Cat.logError(e);
				}
			}
		}
	}

}
