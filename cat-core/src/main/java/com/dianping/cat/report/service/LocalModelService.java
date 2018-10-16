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
package com.dianping.cat.report.service;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.mvc.ApiPayload;

public abstract class LocalModelService<T> implements Initializable {

	public static final int DEFAULT_SIZE = 32 * 1024;

	@Inject
	protected ServerConfigManager m_configManager;

	@Inject
	private MessageConsumer m_consumer;

	private int m_analyzerCount = 2;

	private String m_defaultDomain = Constants.CAT;

	private String m_name;

	public LocalModelService(String name) {
		m_name = name;
	}

	public abstract String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception;

	public int getAnalyzerCount() {
		return m_analyzerCount;
	}

	public String getName() {
		return m_name;
	}

	@SuppressWarnings("unchecked")
	protected List<T> getReport(ModelPeriod period, String domain) throws Exception {
		List<MessageAnalyzer> analyzers = null;

		if (domain == null || domain.length() == 0) {
			domain = m_defaultDomain;
		}

		if (period.isCurrent()) {
			analyzers = m_consumer.getCurrentAnalyzer(m_name);
		} else if (period.isLast()) {
			analyzers = m_consumer.getLastAnalyzer(m_name);
		}

		if (analyzers == null) {
			return null;
		} else {
			List<T> list = new ArrayList<T>();

			for (MessageAnalyzer a : analyzers) {
				list.add(((AbstractMessageAnalyzer<T>) a).getReport(domain));
			}
			return list;
		}
	}

	public String getReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)	throws Exception {
		try {
			return buildReport(request, period, domain, payload);
		} catch (ConcurrentModificationException e) {
			return buildReport(request, period, domain, payload);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_defaultDomain = m_configManager.getConsoleDefaultDomain();
		m_analyzerCount = m_configManager.getThreadsOfRealtimeAnalyzer(m_name);
	}

	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		return !period.isHistorical();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name);
		sb.append(']');

		return sb.toString();
	}
}
