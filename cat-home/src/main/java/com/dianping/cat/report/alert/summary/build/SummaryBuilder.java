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
package com.dianping.cat.report.alert.summary.build;

import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;

public abstract class SummaryBuilder implements Initializable {

	public Configuration m_configuration;

	protected abstract Map<Object, Object> generateModel(String domain, Date date);

	public String generateHtml(String domain, Date date) {
		Map<Object, Object> dataMap = generateModel(domain, date);
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate(getTemplateAddress());
			t.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return sw.toString();
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

	protected abstract String getTemplateAddress();

	public abstract String getID();
}
