package com.dianping.cat.report.task.alert.sender.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.report.task.alert.manager.AlertManager;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.decorator.DecoratorManager;
import com.dianping.cat.report.task.alert.sender.receiver.Seeker;
import com.dianping.cat.system.config.AlertPolicyManager;

public class DispatcherManager extends ContainerHolder implements Initializable {

	@Inject
	private AlertPolicyManager m_policyManager;

	@Inject
	private DecoratorManager m_decoratorManager;

	@Inject
	private Seeker m_seeker;

	@Inject
	protected AlertManager m_alertManager;

	private Map<String, Dispatcher> m_dispatchers = new HashMap<String, Dispatcher>();

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

	@Override
	public void initialize() throws InitializationException {
		m_dispatchers = lookupMap(Dispatcher.class);
	}

}
