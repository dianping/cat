package com.dianping.cat.report.task.alert.sender.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.report.task.alert.manager.AlertManager;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.decorator.DecoratorManager;
import com.dianping.cat.report.task.alert.sender.receiver.Seeker;
import com.dianping.cat.system.config.AlertPolicyManager;

public class DispatcherManager implements Initializable {

	@Inject
	private AlertPolicyManager m_policyManager;

	@Inject
	private DecoratorManager m_decoratorManager;

	@Inject
	private Seeker m_seeker;

	@Inject
	protected AlertManager m_alertManager;

	@Inject(type = Dispatcher.class, value = MailDispatcher.ID)
	protected Dispatcher m_mailDispatcher;

	@Inject(type = Dispatcher.class, value = WeixinDispatcher.ID)
	protected Dispatcher m_weixinDispatcher;

	@Inject(type = Dispatcher.class, value = SmsDispatcher.ID)
	protected Dispatcher m_smsDispatcher;

	private Map<String, Dispatcher> m_dispatchers = new HashMap<String, Dispatcher>();

	@Override
	public void initialize() throws InitializationException {
		m_dispatchers.put(m_mailDispatcher.getId(), m_mailDispatcher);
		m_dispatchers.put(m_weixinDispatcher.getId(), m_weixinDispatcher);
		m_dispatchers.put(m_smsDispatcher.getId(), m_smsDispatcher);
	}

	public boolean send(AlertEntity alert) {
		String type = alert.getType();
		String group = alert.getGroup();
		String level = alert.getLevel();

		String channels = m_policyManager.queryChannels(type, group, level);

		for (AlertChannel channel : AlertChannel.values()) {
			String channelName = channel.getName();
			if (channels.contains(channelName)) {
				Pair<String, String> pair = m_decoratorManager.generateTitleAndContent(alert, channelName);
				List<String> receivers = m_seeker.queryReceivers(alert.getProductline(), channel, type);
				AlertMessageEntity message = new AlertMessageEntity(group, pair.getKey(), pair.getValue(), receivers);

				m_alertManager.storeAlert(alert, message);

				Dispatcher dispatcher = m_dispatchers.get(channelName);
				dispatcher.send(message);
			}
		}

		return false;
	}

	public void setMailDispatcher(Dispatcher dispatcher) {
		m_mailDispatcher = dispatcher;
	}

	public void setSmsDispatcher(Dispatcher dispatcher) {
		m_smsDispatcher = dispatcher;
	}

	public void setWeixinDispatcher(Dispatcher dispatcher) {
		m_weixinDispatcher = dispatcher;
	}

}
