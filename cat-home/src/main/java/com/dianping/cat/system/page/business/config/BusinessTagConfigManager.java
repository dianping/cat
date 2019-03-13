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
package com.dianping.cat.system.page.business.config;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.config.BusinessConfigDao;
import com.dianping.cat.core.config.BusinessConfigEntity;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;
import com.dianping.cat.home.business.transform.DefaultSaxParser;

public class BusinessTagConfigManager implements Initializable {

	public final static String TAG_CONFIG = "tag";

	@Inject
	private BusinessConfigDao m_configDao;

	private int m_configId;

	private BusinessTagConfig m_tagConfig;

	public Set<String> findAllTags() {
		return m_tagConfig.getTags().keySet();
	}

	public Tag findTag(String id) {
		return m_tagConfig.findTag(id);
	}

	public Map<String, Set<String>> findTagByDomain(String domain) {
		Map<String, Set<String>> domainTags = new HashMap<String, Set<String>>();
		Map<String, Tag> tags = m_tagConfig.getTags();

		for (Tag tag : tags.values()) {
			List<BusinessItem> items = tag.getBusinessItems();

			for (BusinessItem item : items) {
				if (item.getDomain().equals(domain)) {
					String id = item.getItemId();
					Set<String> itemTags = domainTags.get(id);

					if (itemTags == null) {
						itemTags = new HashSet<String>();
						domainTags.put(id, itemTags);
					}

					itemTags.add(tag.getId());
				}
			}
		}
		return domainTags;
	}

	public BusinessTagConfig getConfig() {
		return m_tagConfig;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			List<BusinessConfig> result = m_configDao.findByName(TAG_CONFIG, BusinessConfigEntity.READSET_FULL);

			if (result.size() > 0) {
				BusinessConfig config = result.get(0);
				m_configId = config.getId();
				m_tagConfig = DefaultSaxParser.parse(config.getContent());
			} else {
				m_tagConfig = new BusinessTagConfig();

				BusinessConfig config = m_configDao.createLocal();

				config.setName(TAG_CONFIG);
				config.setDomain(Constants.CAT);
				config.setContent(m_tagConfig.toString());
				config.setUpdatetime(new Date());
				m_configDao.insert(config);

				m_configId = config.getId();
			}

		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public boolean store(String xml) {
		try {
			m_tagConfig = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				BusinessConfig config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(TAG_CONFIG);
				config.setContent(m_tagConfig.toString());
				config.setUpdatetime(new Date());
				m_configDao.updateByPK(config, BusinessConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
