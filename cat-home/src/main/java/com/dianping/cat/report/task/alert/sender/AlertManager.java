package com.dianping.cat.report.task.alert.sender;

import java.util.List;
import java.util.concurrent.BlockingQueue;
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

	private BlockingQueue<AlertEntity> m_alerts = new LinkedBlockingDeque<AlertEntity>(10000);

	private boolean send(AlertEntity alert) {
		boolean result = false;
		String type = alert.getType();
		String group = alert.getGroup();
		String level = alert.getLevel();
		List<AlertChannel> channels = m_policyManager.queryChannels(type, group, level);

		for (AlertChannel channel : channels) {
			Pair<String, String> pair = m_decoratorManager.generateTitleAndContent(alert);
			String title = pair.getKey();
			String content = m_splitterManager.process(pair.getValue(), channel);
			List<String> receivers = m_contactorManager.queryReceivers(group, channel, type);
			AlertMessageEntity message = new AlertMessageEntity(group, title, content, receivers);

			m_alertEntityService.storeAlert(alert, message);

			if (m_senderManager.sendAlert(channel, type, message)) {
				result = true;
			}
		}
		return result;
	}

	public boolean addAlert(AlertEntity alert) {
		String type = alert.getType();
		String group = alert.getGroup();
		Cat.logEvent("Alert:" + type, group, Event.SUCCESS, alert.toString());

		return m_alerts.offer(alert);
	}

	private class SendExecutor implements Task {
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
		public String getName() {
			return "send-executor";
		}

		@Override
		public void shutdown() {
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new SendExecutor());
	}

}
