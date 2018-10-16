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
package com.dianping.cat.build;

import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.rule.DefaultDataChecker;
import com.dianping.cat.alarm.spi.sender.*;
import com.dianping.cat.alarm.spi.spliter.*;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import java.util.ArrayList;
import java.util.List;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		all.add(A(SenderConfigManager.class));

		all.add(A(DefaultDataChecker.class));
		all.add(A(DecoratorManager.class));
		all.add(A(ContactorManager.class));

		all.add(A(AlertPolicyManager.class));

		all.add(C(Spliter.class, MailSpliter.ID, MailSpliter.class));

		all.add(C(Spliter.class, SmsSpliter.ID, SmsSpliter.class));

		all.add(C(Spliter.class, WeixinSpliter.ID, WeixinSpliter.class));

		all.add(C(Spliter.class, DXSpliter.ID, DXSpliter.class));

		all.add(A(SpliterManager.class));

		all.add(C(Sender.class, MailSender.ID, MailSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, SmsSender.ID, SmsSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, WeixinSender.ID, WeixinSender.class).req(SenderConfigManager.class));

		all.add(A(SenderManager.class));

		all.add(A(AlertManager.class));

		all.add(A(AlertService.class));

		all.add(A(AlertConfigManager.class));

		return all;
	}
}
