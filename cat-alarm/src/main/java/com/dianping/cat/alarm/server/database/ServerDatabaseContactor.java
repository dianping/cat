/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dianping.cat.alarm.server.database;

import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import org.unidal.lookup.annotation.Inject;

import java.util.Collections;
import java.util.List;

public class ServerDatabaseContactor extends ProjectContactor implements Contactor {

	public static final String ID = AlertType.SERVER_DATABASE.getName();

	@Inject
	protected AlertConfigManager m_configManager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List<String> queryDXContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultDXReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultMailReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultSMSReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		Receiver receiver = m_configManager.queryReceiverById(getId());

		if (receiver != null && receiver.isEnable()) {
			return buildDefaultWeixinReceivers(receiver);
		} else {
			return Collections.emptyList();
		}
	}

}
