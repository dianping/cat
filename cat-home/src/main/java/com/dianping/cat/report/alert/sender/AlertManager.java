package com.dianping.cat.report.alert.sender;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.config.AlertPolicyManager;
import com.dianping.cat.report.alert.sender.decorator.DecoratorManager;
import com.dianping.cat.report.alert.sender.receiver.ContactorManager;
import com.dianping.cat.report.alert.sender.sender.SenderManager;
import com.dianping.cat.report.alert.sender.spliter.SpliterManager;
import com.dianping.cat.report.alert.service.AlertEntityService;

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

	@Inject
	private ServerConfigManager m_configManager;

	private static final int MILLIS1MINUTE = 1 * 60 * 1000;

	private BlockingQueue<AlertEntity> m_alerts = new LinkedBlockingDeque<AlertEntity>(10000);

	private Map<String, AlertEntity> m_unrecoveredAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	private Map<String, AlertEntity> m_sendedAlerts = new ConcurrentHashMap<String, AlertEntity>(1000);

	public boolean addAlert(AlertEntity alert) {
		String type = alert.getType();
		String group = alert.getGroup();
		Cat.logEvent("Alert:" + type, group, Event.SUCCESS, alert.toString());

		if (m_configManager.isAlertMachine()) {
			return m_alerts.offer(alert);
		} else {
			return true;
		}
	}

	private String generateTypeStr(String type) {
		AlertType typeByName = AlertType.getTypeByName(type);

		switch (typeByName) {
		case Business:
			return "业务告警";
		case Network:
			return "网络告警";
		case System:
			return "系统告警";
		case Exception:
			return "异常告警";
		case ThirdParty:
			return "第三方告警";
		case FrontEndException:
			return "前端告警";
		case App:
			return "手机端告警";
		case Web:
			return "web告警";
		case HeartBeat:
			return "心跳告警";
		case Transaction:
			return "transaction告警";
		case DataBase:
			return "数据库系统告警";
		case STORAGE_SQL:
			return "数据库访问告警";
		case STORAGE_CACHE:
			return "缓存访问告警";
		}
		return type;
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

	private boolean send(AlertEntity alert) {
		boolean result = false;
		String type = alert.getType();
		String group = alert.getGroup();
		String level = alert.getLevel();
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
		AlertMessageEntity message = null;

		for (AlertChannel channel : channels) {
			String rawContent = pair.getValue();
			if (suspendMinute > 0) {
				rawContent = rawContent + "<br/>[告警间隔时间]" + suspendMinute + "分钟";
			}
			String content = m_splitterManager.process(rawContent, channel);
			List<String> receivers = m_contactorManager.queryReceivers(alert.getContactGroup(), channel, type);
			message = new AlertMessageEntity(group, title, type, content, receivers);

			if (m_senderManager.sendAlert(channel, message)) {
				result = true;
			}
		}

		String dbContent = Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(pair.getValue())
		      .replaceAll("");

		if (message == null) {
			message = new AlertMessageEntity(group, title, type, "", null);
		}
		message.setContent(dbContent);
		m_alertEntityService.storeAlert(alert, message);
		return result;
	}

	private boolean sendRecoveryMessage(AlertEntity alert, String currentMinute) {
		String type = alert.getType();
		String group = alert.getGroup();
		String level = alert.getLevel();
		List<AlertChannel> channels = m_policyManager.queryChannels(type, group, level);

		for (AlertChannel channel : channels) {
			String title = "[告警恢复] [告警类型 " + generateTypeStr(type) + "][" + group + " " + alert.getMetric() + "]";
			String content = "[告警已恢复][恢复时间]" + currentMinute;
			List<String> receivers = m_contactorManager.queryReceivers(alert.getContactGroup(), channel, type);
			AlertMessageEntity message = new AlertMessageEntity(group, title, type, content, receivers);

			if (m_senderManager.sendAlert(channel, message)) {
				return true;
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
			String type = alert.getType();
			String group = alert.getGroup();
			String level = alert.getLevel();

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
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
