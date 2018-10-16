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
package com.dianping.cat.alarm.spi.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.service.ProjectService;

public abstract class ProjectContactor extends DefaultContactor implements Contactor {

	@Inject
	protected ProjectService m_projectService;

	@Inject
	protected AlertConfigManager m_configManager;

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));

			if (StringUtils.isNotEmpty(id)) {
				Project project = m_projectService.findByDomain(id);

				if (project != null) {
					mailReceivers.addAll(split(project.getEmail()));
				}
			}
			return mailReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));

			if (StringUtils.isNotEmpty(id)) {
				Project project = m_projectService.findByDomain(id);

				if (project != null) {
					smsReceivers.addAll(split(project.getPhone()));
				}
			}
			return smsReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));

			if (StringUtils.isNotEmpty(id)) {
				Project project = m_projectService.findByDomain(id);

				if (project != null) {
					weixinReceivers.addAll(split(project.getEmail()));
				}
			}
			return weixinReceivers;
		}
	}

	@Override
	public List<String> queryDXContactors(String id) {
		List<String> receivers = new ArrayList<String>();
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return receivers;
		} else {
			receivers.addAll(buildDefaultDXReceivers(receiver));

			if (StringUtils.isNotEmpty(id)) {
				Project project = m_projectService.findByDomain(id);

				if (project != null) {
					receivers.addAll(split(project.getEmail()));
				}
			}
			return receivers;
		}
	}

}
