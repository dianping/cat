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
package com.dianping.cat.system.page.config;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.web.mvc.annotation.PreInboundActionMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dal.report.ConfigModification;
import com.dianping.cat.home.dal.report.ConfigModificationDao;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.config.processor.AlertConfigProcessor;
import com.dianping.cat.system.page.config.processor.DependencyConfigProcessor;
import com.dianping.cat.system.page.config.processor.EventConfigProcessor;
import com.dianping.cat.system.page.config.processor.ExceptionConfigProcessor;
import com.dianping.cat.system.page.config.processor.GlobalConfigProcessor;
import com.dianping.cat.system.page.config.processor.HeartbeatConfigProcessor;
import com.dianping.cat.system.page.config.processor.StorageConfigProcessor;
import com.dianping.cat.system.page.config.processor.TransactionConfigProcessor;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private GlobalConfigProcessor m_globalConfigProcessor;

	@Inject
	private DependencyConfigProcessor m_topologyConfigProcessor;

	@Inject
	private ExceptionConfigProcessor m_exceptionConfigProcessor;

	@Inject
	private HeartbeatConfigProcessor m_heartbeatConfigProcessor;

	@Inject
	private AlertConfigProcessor m_alertConfigProcessor;

	@Inject
	private TransactionConfigProcessor m_transactionConfigProcessor;

	@Inject
	private EventConfigProcessor m_eventConfigProcessor;

	@Inject
	private StorageConfigProcessor m_storageConfigProcessor;

	@Inject
	private ConfigModificationDao m_configModificationDao;

	@Override
	@PreInboundActionMeta("login")
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "config")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@PreInboundActionMeta("login")
	@OutboundActionMeta(name = "config")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.CONFIG);
		Action action = payload.getAction();

		storeModifyInfo(ctx, payload);
		model.setAction(action);
		switch (action) {
		case PROJECT_ALL:
		case PROJECT_ADD:
		case PROJECT_UPDATE_SUBMIT:
		case PROJECT_DELETE:
		case DOMAIN_GROUP_CONFIGS:
		case DOMAIN_GROUP_CONFIG_UPDATE:
		case DOMAIN_GROUP_CONFIG_DELETE:
		case DOMAIN_GROUP_CONFIG_SUBMIT:
		case ROUTER_CONFIG_UPDATE:
		case ALERT_SENDER_CONFIG_UPDATE:
		case STORAGE_GROUP_CONFIG_UPDATE:
		case SERVER_FILTER_CONFIG_UPDATE:
		case SERVER_CONFIG_UPDATE:
		case ALL_REPORT_CONFIG:
		case SAMPLE_CONFIG_UPDATE:
		case REPORT_RELOAD_CONFIG_UPDATE:
			m_globalConfigProcessor.process(action, payload, model);
			break;

		case TOPOLOGY_GRAPH_NODE_CONFIG_LIST:
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE:
		case TOPOLOGY_GRAPH_NODE_CONFIG_ADD_OR_UPDATE_SUBMIT:
		case TOPOLOGY_GRAPH_NODE_CONFIG_DELETE:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_LIST:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_ADD_OR_UPDATE_SUBMIT:
		case TOPOLOGY_GRAPH_EDGE_CONFIG_DELETE:
		case TOPO_GRAPH_FORMAT_CONFIG_UPDATE:
			m_topologyConfigProcessor.process(action, payload, model);
			break;

		case EXCEPTION:
		case EXCEPTION_THRESHOLD_DELETE:
		case EXCEPTION_THRESHOLD_UPDATE:
		case EXCEPTION_THRESHOLD_ADD:
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
		case EXCEPTION_EXCLUDE_DELETE:
		case EXCEPTION_EXCLUDE_ADD:
		case EXCEPTION_EXCLUDE_UPDATE_SUBMIT:
			m_exceptionConfigProcessor.process(action, payload, model);
			break;

		case HEARTBEAT_RULE_CONFIG_LIST:
		case HEARTBEAT_RULE_ADD_OR_UPDATE:
		case HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT:
		case HEARTBEAT_RULE_DELETE:
		case HEARTBEAT_DISPLAY_POLICY:
			m_heartbeatConfigProcessor.process(action, payload, model);
			break;

		case STORAGE_RULE:
		case STORAGE_RULE_ADD_OR_UPDATE:
		case STORAGE_RULE_ADD_OR_UPDATE_SUBMIT:
		case STORAGE_RULE_DELETE:
			m_storageConfigProcessor.process(action, payload, model);
			break;

		case TRANSACTION_RULE:
		case TRANSACTION_RULE_ADD_OR_UPDATE:
		case TRANSACTION_RULE_ADD_OR_UPDATE_SUBMIT:
		case TRANSACTION_RULE_DELETE:
			m_transactionConfigProcessor.process(action, payload, model);
			break;

		case EVENT_RULE:
		case EVENT_RULE_ADD_OR_UPDATE:
		case EVENT_RULE_ADD_OR_UPDATE_SUBMIT:
		case EVENT_RULE_DELETE:
			m_eventConfigProcessor.process(action, payload, model);
			break;

		case ALERT_DEFAULT_RECEIVERS:
		case ALERT_POLICY:
			m_alertConfigProcessor.process(action, payload, model);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	public void store(String userName, String accountName, Payload payload) {
		ConfigModification modification = m_configModificationDao.createLocal();

		modification.setUserName(userName);
		modification.setAccountName(accountName);
		modification.setActionName(payload.getAction().getName());
		modification.setDate(new Date());
		modification.setArgument(new JsonBuilder().toJson(payload));

		try {
			m_configModificationDao.insert(modification);
		} catch (Exception ex) {
			Cat.logError(ex);
		}
	}

	private void storeModifyInfo(Context ctx, Payload payload) {
		Cookie cookie = ctx.getCookie("ct");

		if (cookie != null) {
			String cookieValue = cookie.getValue();

			try {
				String[] values = cookieValue.split("\\|");
				String userName = values[0];
				String account = values[1];

				if (userName.startsWith("\"")) {
					userName = userName.substring(1, userName.length() - 1);
				}
				userName = URLDecoder.decode(userName, "UTF-8");

				store(userName, account, payload);
			} catch (Exception ex) {
				Cat.logError("store cookie fail:" + cookieValue, new RuntimeException());
			}
		}
	}

}
