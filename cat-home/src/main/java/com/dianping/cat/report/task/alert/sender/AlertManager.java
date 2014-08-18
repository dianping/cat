package com.dianping.cat.report.task.alert.sender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.decorator.DecoratorManager;
import com.dianping.cat.report.task.alert.sender.receiver.ContactorManager;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.dianping.cat.report.task.alert.sender.spliter.SpliterManager;
import com.dianping.cat.report.task.alert.service.AlertEntityService;
import com.dianping.cat.system.config.AlertPolicyManager;

public class AlertManager implements Initializable {

	@Inject
	private AlertPolicyManager m_policyManager;

	@Inject
	private DecoratorManager m_decoratorManager;

	@Inject
	private ContactorManager m_contactorManager;

	@Inject
	protected SpliterManager m_splitterManager;

	@Inject
	protected SenderManager m_senderManager;

	@Inject
	protected AlertEntityService m_alertEntityService;

	private static final int MILLIS1MINUTE = 1 * 60 * 1000;

	private BlockingQueue<AlertEntity> m_alerts = new LinkedBlockingDeque<AlertEntity>(10000);

	private Map<String, AlertEntity> m_unrecoveredAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	private Map<String, AlertEntity> m_sendedAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	public boolean addAlert(AlertEntity alert) {
		String type = alert.getType();
		String group = alert.getGroup();
		Cat.logEvent("Alert:" + type, group, Event.SUCCESS, alert.toString());

		return m_alerts.offer(alert);
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new SendExecutor());
		Threads.forGroup("Cat").start(new RecoveryAnnouncer());
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

	private boolean send(AlertEntity alert) {
		boolean result = false;
		String type = alert.getType();
		String group = alert.getGroup();
		String level = alert.getLevel();
		String alertKey = alert.getKey();
		List<AlertChannel> channels = m_policyManager.queryChannels(type, group, level);
		int recoverMinute = m_policyManager.queryRecoverMinute(type, group, level);
		int suspendMinute = m_policyManager.querySuspendMinute(type, group, level);

		if (recoverMinute > 0) {
			String key = recoverMinute + ":" + alertKey;
			m_unrecoveredAlerts.put(key, alert);
		}

		if (suspendMinute > 0) {
			if (isSuspend(alertKey, suspendMinute)) {
				return true;
			} else {
				m_sendedAlerts.put(alertKey, alert);
			}
		}

		for (AlertChannel channel : channels) {
			Pair<String, String> pair = m_decoratorManager.generateTitleAndContent(alert);
			String title = pair.getKey();
			String content = m_splitterManager.process(pair.getValue(), channel);
			List<String> receivers = m_contactorManager.queryReceivers(group, channel, type);
			AlertMessageEntity message = new AlertMessageEntity(group, title, type, content, receivers);

			

			if (m_senderManager.sendAlert(channel, message)) {
				result = true;
			}
		}
		return result;
	}

	private boolean sendRecoveryMessage(AlertEntity alert) {
		boolean result = false;
		String type = alert.getType();
		String group = alert.getGroup();
		String level = alert.getLevel();
		List<AlertChannel> channels = m_policyManager.queryChannels(type, group, level);

		for (AlertChannel channel : channels) {
			String title = "[告警恢复] [" + group + " " + alert.getMetric() + "]";
			String content = "[告警已恢复]";
			List<String> receivers = m_contactorManager.queryReceivers(group, channel, type);
			AlertMessageEntity message = new AlertMessageEntity(group, title, type, content, receivers);

			if (m_senderManager.sendAlert(channel, message)) {
				result = true;
			}
		}
		return result;
	}

	private class RecoveryAnnouncer implements Task {

		@Override
		public String getName() {
			return "recovery-announcer";
		}

		@Override
		public void run() {
			while (true) {
				long currentTime = System.currentTimeMillis();
				List<String> recoveredItems = new ArrayList<String>();

				for (Entry<String, AlertEntity> entry : m_unrecoveredAlerts.entrySet()) {
					try {
						String key = entry.getKey();
						AlertEntity alert = entry.getValue();
						long alertTime = alert.getDate().getTime();
						int alreadyMinutes = (int) ((currentTime - alertTime) / MILLIS1MINUTE);
						int requiredMinutes = Integer.parseInt(key.split(":")[0]);

						if (alreadyMinutes >= requiredMinutes) {
							recoveredItems.add(key);
							sendRecoveryMessage(alert);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}

				for (String key : recoveredItems) {
					m_unrecoveredAlerts.remove(key);
				}

				long duration = System.currentTimeMillis() - currentTime;
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
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
