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

package com.dianping.cat.alarm.app.crash;

import com.dianping.cat.alarm.crash.entity.ExceptionLimit;
import com.dianping.cat.alarm.receiver.entity.Receiver;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import org.unidal.lookup.annotation.Inject;

import java.util.ArrayList;
import java.util.List;

public class CrashContactor extends ProjectContactor implements Contactor {
	public static final String ID = AlertType.CRASH.getName();

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	@Inject
	protected CrashRuleConfigManager m_crashAlarmRuleManager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			ExceptionLimit rule = m_crashAlarmRuleManager.queryExceptionLimit(id);

			if (rule != null) {
				mailReceivers.addAll(split(rule.getMails()));
			}

			return mailReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));
			ExceptionLimit rule = m_crashAlarmRuleManager.queryExceptionLimit(id);

			if (rule != null) {
				weixinReceivers.addAll(split(rule.getMails()));
			}

			return weixinReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));

			return smsReceivers;
		}
	}

	@Override
	public List<String> queryDXContactors(String id) {
		List<String> receivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return receivers;
		} else {
			receivers.addAll(buildDefaultDXReceivers(receiver));
			ExceptionLimit rule = m_crashAlarmRuleManager.queryExceptionLimit(id);

			if (rule != null) {
				receivers.addAll(split(rule.getMails()));
			}

			return receivers;
		}
	}

}
