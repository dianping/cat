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
package com.dianping.cat.alarm.spi;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.alarm.spi.spliter.SpliterManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Named
public class AlertManager implements Initializable {

	private static final int MILLIS1MINUTE = 60 * 1000;

	@Inject
	protected SpliterManager m_splitterManager;

	@Inject
	protected SenderManager m_senderManager;

	@Inject
	protected AlertService m_alertService;

	@Inject
	private AlertPolicyManager m_policyManager;

	@Inject
	private DecoratorManager m_decoratorManager;

	@Inject
	private ContactorManager m_contactorManager;

	@Inject
	private ServerConfigManager m_configManager;

	private BlockingQueue<AlertEntity> m_alerts = new LinkedBlockingDeque<AlertEntity>(10000);

	private Map<String, AlertEntity> m_unrecoveredAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	private Map<String, AlertEntity> m_sendedAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	private ConcurrentHashMap<AlertEntity, Long> m_alertMap = new ConcurrentHashMap<AlertEntity, Long>();

	public boolean addAlert(AlertEntity entity) {
		m_alertMap.put(entity, entity.getDate().getTime());

		String group = entity.getGroup();
		Cat.logEvent("Alert:" + entity.getType().getName(), group, Event.SUCCESS, entity.toString());

		if (m_configManager.isAlertMachine()) {
			return m_alerts.offer(entity);
		} else {
			return true;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("cat").start(new SendExecutor());
		Threads.forGroup("cat").start(new RecoveryAnnouncer());
	}

	public boolean isSuspend(String alertKey, int suspendMinute) {
		AlertEntity sendedAlert = m_sendedAlerts.get(alertKey);

		if (sendedAlert != null) {
			long duration = System.currentTimeMillis() - sendedAlert.getDate().getTime();

			if (duration / MILLIS1MINUTE < suspendMinute) {
				Cat.logEvent("SuspendAlert", alertKey, Event.SUCCESS, null);
				return true;
			}
		}
		return false;
	}

	public List<AlertEntity> queryLastestAlarmKey(int minute) {
		List<AlertEntity> keys = new ArrayList<AlertEntity>();
		long currentTimeMillis = System.currentTimeMillis();

		for (Entry<AlertEntity, Long> entry : m_alertMap.entrySet()) {
			Long value = entry.getValue();

			if (currentTimeMillis - value < TimeHelper.ONE_MINUTE * minute) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

	//List去重
	private void removeDuplicate(List<String> list) {
		LinkedHashSet<String> set = new LinkedHashSet<String>(list.size());
		set.addAll(list);
		list.clear();
		list.addAll(set);
	}

	private boolean send(AlertEntity alert) {
		boolean result = false;
		String type = alert.getType().getName();
		String group = alert.getGroup();
		String level = alert.getLevel().getLevel();
		String alertKey = alert.getKey();
		List<AlertChannel> channels = m_policyManager.queryChannels(type, group, level);
		int suspendMinute = m_policyManager.querySuspendMinute(type, group, level);

		m_unrecoveredAlerts.put(alertKey, alert);

		Pair<String, String> pair = m_decoratorManager.generateTitleAndContent(alert);
		String title = pair.getKey();

		if (suspendMinute > 0) {
			if (isSuspend(alertKey, suspendMinute)) {
				return true;
			} else {
				m_sendedAlerts.put(alertKey, alert);
			}
		}

		SendMessageEntity message = null;

		for (AlertChannel channel : channels) {
			String contactGroup = alert.getContactGroup();
			List<String> receivers = m_contactorManager.queryReceivers(contactGroup, channel, type);
			//去重
			removeDuplicate(receivers);

			if (receivers.size() > 0) {
				String rawContent = pair.getValue();

				if (suspendMinute > 0) {
					rawContent = rawContent + "<br/>[告警间隔时间]" + suspendMinute + "分钟";
				}
				String content = m_splitterManager.process(rawContent, channel);
				message = new SendMessageEntity(group, title, type, content, receivers);

				if (m_senderManager.sendAlert(channel, message)) {
					result = true;
				}
			} else {
				Cat.logEvent("NoneReceiver:" + channel, type + ":" + contactGroup, Event.SUCCESS, null);
			}
		}

		String dbContent = Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(pair.getValue()).replaceAll("");

		if (message == null) {
			message = new SendMessageEntity(group, title, type, "", null);
		}
		message.setContent(dbContent);
		m_alertService.insert(alert, message);
		return result;
	}

	private boolean sendRecoveryMessage(AlertEntity alert, String currentMinute) {
		AlertType alterType = alert.getType();
		String type = alterType.getName();
		String group = alert.getGroup();
		String level = alert.getLevel().getLevel();
		List<AlertChannel> channels = m_policyManager.queryChannels(type, group, level);

		for (AlertChannel channel : channels) {
			String title = "[告警恢复] [告警类型 " + alterType.getTitle() + "][" + group + " " + alert.getMetric() + "]";
			String content = "[告警已恢复][恢复时间]" + currentMinute;
			List<String> receivers = m_contactorManager.queryReceivers(alert.getContactGroup(), channel, type);
			//去重
			removeDuplicate(receivers);

			if (receivers.size() > 0) {
				SendMessageEntity message = new SendMessageEntity(group, title, type, content, receivers);

				if (m_senderManager.sendAlert(channel, message)) {
					return true;
				}
			}
		}

		return false;
	}

	private class RecoveryAnnouncer implements Task {

		private DateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		@Override
		public String getName() {
			return "recovery-announcer";
		}

		private int queryRecoverMinute(AlertEntity alert) {
			String type = alert.getType().getName();
			String group = alert.getGroup();
			String level = alert.getLevel().getLevel();

			return m_policyManager.queryRecoverMinute(type, group, level);
		}

		@Override
		public void run() {
			while (true) {
				long current = System.currentTimeMillis();
				String currentStr = m_sdf.format(new Date(current));
				List<String> recoveredItems = new ArrayList<String>();

				for (Entry<String, AlertEntity> entry : m_unrecoveredAlerts.entrySet()) {
					try {
						String key = entry.getKey();
						AlertEntity alert = entry.getValue();
						int recoverMinute = queryRecoverMinute(alert);
						long alertTime = alert.getDate().getTime();
						int alreadyMinutes = (int) ((current - alertTime) / MILLIS1MINUTE);

						if (alreadyMinutes >= recoverMinute) {
							recoveredItems.add(key);
							sendRecoveryMessage(alert, currentStr);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}

				for (String key : recoveredItems) {
					m_unrecoveredAlerts.remove(key);
				}

				long duration = System.currentTimeMillis() - current;
				if (duration < MILLIS1MINUTE) {
					long lackMills = MILLIS1MINUTE - duration;

					try {
						TimeUnit.MILLISECONDS.sleep(lackMills);
					} catch (InterruptedException e) {
						Cat.logError(e);
					}
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	private class SendExecutor implements Task {
		@Override
		public String getName() {
			return "send-executor";
		}

		@Override
		public void run() {
			while (true) {
				try {
					AlertEntity alert = m_alerts.poll(5, TimeUnit.MILLISECONDS);

					if (alert != null) {
						send(alert);
					}
				} catch (Exception e) {
					Cat.logError(e);
					e.printStackTrace();
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
