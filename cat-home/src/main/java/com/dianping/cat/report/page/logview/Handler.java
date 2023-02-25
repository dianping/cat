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
package com.dianping.cat.report.page.logview;

import javax.servlet.ServletException;
import java.io.IOException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "logview")
	private ModelService<String> m_service;

	@Inject
	private ServerConfigManager m_configManager;

	private boolean checkStorageTime(MessageId msg) {
		long time = msg.getTimestamp();
		long current = TimeHelper.getCurrentDay().getTime();

		if (time > current - TimeHelper.ONE_DAY * m_configManager.getHdfsMaxStorageTime()) {
			return true;
		} else {
			return false;
		}
	}

	private String getLogView(String messageId, boolean waterfall) {
		try {
			if (messageId != null) {
				MessageId id = MessageId.parse(messageId);
				long timestamp = id.getTimestamp();
				ModelRequest request = new ModelRequest(id.getDomain(), timestamp) //
										.setProperty("messageId", messageId) //
										.setProperty("waterfall", String.valueOf(waterfall)) //
										.setProperty("timestamp", String.valueOf(timestamp));

				if (m_service.isEligable(request)) {
					ModelResponse<String> response = m_service.invoke(request);
					String logview = response.getModel();

					return logview;
				} else {
					throw new RuntimeException("Internal error: no eligible logview service registered for " + request + "!");
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}

		return null;
	}

	private String getMessageId(Payload payload) {
		String[] path = payload.getPath();

		if (path != null && path.length > 0) {
			return path[0];
		} else {
			return null;
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "m")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "m")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.LOGVIEW);
		model.setDomain(payload.getDomain());
		model.setDate(payload.getDate());

		String messageId = getMessageId(payload);
		String logView = null;
		MessageId msgId = MessageId.parse(messageId);

		if (checkStorageTime(msgId)) {
			logView = getLogView(messageId, payload.isWaterfall());

			if (logView == null || logView.length() == 0) {
				Cat.logEvent("Logview", msgId.getDomain() + ":Fail", Event.SUCCESS, messageId);
			} else {
				Cat.logEvent("Logview", "Success", Event.SUCCESS, messageId);
			}
		} else {
			Cat.logEvent("Logview", "OldMessage", Event.SUCCESS, messageId);
		}

		switch (payload.getAction()) {
		case VIEW:
			model.setTable(logView);
			break;
		}

		m_jspViewer.view(ctx, model);
	}
}
