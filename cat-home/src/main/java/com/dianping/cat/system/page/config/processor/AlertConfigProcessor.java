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
package com.dianping.cat.system.page.config.processor;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class AlertConfigProcessor {

	@Inject
	private AlertConfigManager m_alertConfigManager;

	@Inject
	private AlertPolicyManager m_alertPolicyManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case ALERT_DEFAULT_RECEIVERS:
			String alertDefaultReceivers = payload.getContent();
			String allOnOrOff = payload.getAllOnOrOff();
			String xmlContent = m_alertConfigManager.buildReceiverContentByOnOff(alertDefaultReceivers, allOnOrOff);

			if (!StringUtils.isEmpty(alertDefaultReceivers)) {
				model.setOpState(m_alertConfigManager.insert(xmlContent));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_alertConfigManager.getAlertConfig().toString()));
			break;
		case ALERT_POLICY:
			String alertPolicy = payload.getContent();

			if (!StringUtils.isEmpty(alertPolicy)) {
				model.setOpState(m_alertPolicyManager.insert(alertPolicy));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_alertPolicyManager.getAlertPolicy().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
