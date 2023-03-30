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
package com.dianping.cat.report.alert.transaction;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.decorator.ProjectDecorator;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class TransactionDecorator extends ProjectDecorator implements Initializable {

	public static final String ID = AlertType.Transaction.getName();

	public Configuration m_configuration;

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> datas = new HashMap<Object, Object>();
		String[] fields = alert.getMetric().split("-");

		datas.put("type", fields[0]);
		datas.put("date", m_format.format(alert.getDate()));
		datas.put("content", alert.getContent());
		datas.put("detail", alert.getIps() != null? alert.getIps().toString() : "");
		datas.put("contactInfo", buildContactInfo(alert.getDomain()));
//		datas.put("name", fields[1]);

		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("transactionAlert.ftl");
			t.process(datas, sw);
		} catch (Exception e) {
			Cat.logError("build front end content error:" + alert.toString(), e);
		}

		return sw.toString();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		return "【" + alert.getLevel().getText() + "】" + alert.getDomain();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void initialize() throws InitializationException {
		m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(this.getClass(), "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}
	}
}
