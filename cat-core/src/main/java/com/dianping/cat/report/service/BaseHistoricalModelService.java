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

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public abstract class BaseHistoricalModelService<T> extends ModelServiceWithCalSupport
						implements ModelService<T>,	Initializable {

	@Inject
	protected ServerConfigManager m_configManager;

	private boolean m_localMode = true;

	private String m_name;

	public BaseHistoricalModelService(String name) {
		m_name = name;
	}

	protected abstract T buildModel(ModelRequest request) throws Exception;

	@Override
	public String getName() {
		return m_name;
	}

	@Override
	public void initialize() throws InitializationException {
		m_localMode = m_configManager.isLocalMode();
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());
		t.addData("thread", Thread.currentThread());

		try {
			T model = buildModel(request);

			response.setModel(model);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		return request.getPeriod().isHistorical();
	}

	protected boolean isLocalMode() {
		return m_localMode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name).append(']');

		return sb.toString();
	}
}
