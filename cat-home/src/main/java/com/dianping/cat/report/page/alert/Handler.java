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
package com.dianping.cat.report.page.alert;

import javax.servlet.ServletException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Splitters;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.Alert;
import com.dianping.cat.alarm.AlertDao;
import com.dianping.cat.alarm.AlertEntity;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private SenderManager m_senderManager;

	@Inject
	private AlertDao m_alertDao;

	private Alert buildAlertEntity(Payload payload) {
		Alert alertEntity = new Alert();

		alertEntity.setAlertTime(payload.getAlertTime());
		alertEntity.setCategory(payload.getCategory());
		alertEntity.setContent(payload.getContent());
		alertEntity.setDomain(payload.getDomain());
		alertEntity.setMetric(payload.getMetric());
		alertEntity.setType(payload.getLevel());
		return alertEntity;
	}

	private Map<String, AlertMinute> generateAlertMinutes(List<Alert> alerts) {
		DateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		Map<String, AlertMinute> alertMinutes = new LinkedHashMap<String, AlertMinute>();

		for (Alert alert : alerts) {
			String time = format.format(alert.getAlertTime());
			AlertMinute alertMinute = alertMinutes.get(time);

			if (alertMinute == null) {
				alertMinute = new AlertMinute(time);

				alertMinutes.put(time, alertMinute);
			}
			alertMinute.addAlert(alert);
		}

		return alertMinutes;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "alert")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "alert")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case ALERT:
			List<String> receivers = Splitters.by(",").noEmptyItem().split(payload.getReceivers());
			if (receivers == null || receivers.size() == 0) {
				setAlertResult(model, 0);
			} else {
				SendMessageEntity message = new SendMessageEntity(payload.getGroup(), payload.getTitle(),	payload.getType(),
										payload.getContent(), receivers);

				try {
					boolean result = m_senderManager.sendAlert(AlertChannel.findByName(payload.getChannel()), message);
					if (result) {
						setAlertResult(model, 1);
					} else {
						setAlertResult(model, 2);
					}
				} catch (NullPointerException ex) {
					setAlertResult(model, 3);
				}
			}
			break;
		case INSERT:
			if (StringUtils.isEmpty(payload.getDomain())) {
				setAlertResult(model, 4);
			} else {
				Alert alertEntity = buildAlertEntity(payload);

				try {
					int count = m_alertDao.insert(alertEntity);

					if (count == 0) {
						setAlertResult(model, 5);
					} else {
						setAlertResult(model, 1);
					}
				} catch (DalException e) {
					setAlertResult(model, 5);
					Cat.logError(e);
				}
			}
			break;
		case VIEW:
			Date startTime = payload.getStartTime();
			Date endTime = payload.getEndTime();
			String domain = payload.getDomain();
			String alertTypeStr = payload.getAlertType();
			List<Alert> alerts;
			try {
				if (StringUtils.isEmpty(alertTypeStr)) {
					alerts = m_alertDao.queryAlertsByTimeDomain(startTime, endTime, domain, AlertEntity.READSET_FULL);
				} else {
					alerts = m_alertDao.queryAlertsByTimeDomainCategories(startTime, endTime, domain,	payload.getAlertTypeArray(),
											AlertEntity.READSET_FULL);
				}
			} catch (DalException e) {
				alerts = new ArrayList<Alert>();
				Cat.logError(e);
			}
			model.setAlertMinutes(generateAlertMinutes(alerts));
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ALERT);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void setAlertResult(Model model, int status) {
		switch (status) {
		case 0:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"lack receivers\"}");
			break;
		case 1:
			model.setAlertResult("{\"status\":200}");
			break;
		case 2:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"send failed, please retry again\"}");
			break;
		case 3:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"send failed, please check your channel argument\"}");
			break;
		case 4:
			model.setAlertResult("{\"status\":500, \"errorMessage\":\"lack domain\"}");
			break;
		case 5:
			model.setAlertResult("{\"status\":500}");
			break;
		}
	}

	public class AlertDomain {

		private String m_name;

		private Map<String, List<Alert>> m_alertsByCategory = new HashMap<String, List<Alert>>();

		public AlertDomain(String name) {
			m_name = name;
		}

		public void addAlert(Alert alert) {
			String category = alert.getCategory();
			List<Alert> alerts = m_alertsByCategory.get(category);

			if (alerts == null) {
				alerts = new ArrayList<Alert>();

				m_alertsByCategory.put(category, alerts);
			}
			alerts.add(alert);
		}

		public Map<String, List<Alert>> getAlertCategories() {
			return m_alertsByCategory;
		}

		public int getCount() {
			int count = 0;

			for (List<Alert> alerts : m_alertsByCategory.values()) {
				count += alerts.size();
			}
			return count;
		}

		public String getName() {
			return m_name;
		}

	}

	public class AlertMinute {

		private String m_time;

		private Map<String, AlertDomain> m_domains = new HashMap<String, AlertDomain>();

		public AlertMinute(String time) {
			m_time = time;
		}

		public void addAlert(Alert alert) {
			String domain = alert.getDomain();
			AlertDomain alertDomain = m_domains.get(domain);

			if (alertDomain == null) {
				alertDomain = new AlertDomain(domain);

				m_domains.put(domain, alertDomain);
			}

			alertDomain.addAlert(alert);
		}

		public List<AlertDomain> getAlertDomains() {
			List<AlertDomain> alertDomains = new ArrayList<Handler.AlertDomain>(m_domains.values());

			Collections.sort(alertDomains, new Comparator<AlertDomain>() {
				@Override
				public int compare(AlertDomain domain1, AlertDomain domain2) {
					return domain2.getCount() - domain1.getCount();
				}
			});
			return alertDomains;
		}

		public String getTime() {
			return m_time;
		}

	}

}
