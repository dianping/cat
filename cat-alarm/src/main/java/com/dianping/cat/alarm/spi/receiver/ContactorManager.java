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

import com.dianping.cat.alarm.spi.AlertChannel;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class ContactorManager extends ContainerHolder implements Initializable {

	private Map<String, Contactor> m_contactors = new HashMap<String, Contactor>();

	@Override
	public void initialize() throws InitializationException {
		m_contactors = lookupMap(Contactor.class);
	}

	public List<String> queryReceivers(String group, AlertChannel channel, String type) {
		Contactor contactor = m_contactors.get(type);

		if (AlertChannel.MAIL == channel) {
			return contactor.queryEmailContactors(group);
		} else if (AlertChannel.SMS == channel) {
			return contactor.querySmsContactors(group);
		} else if (AlertChannel.WEIXIN == channel) {
			return contactor.queryWeiXinContactors(group);
		} else if (AlertChannel.DX == channel) {
			return contactor.queryDXContactors(group);
		} else {
			throw new RuntimeException("unsupported channel");
		}
	}

}
