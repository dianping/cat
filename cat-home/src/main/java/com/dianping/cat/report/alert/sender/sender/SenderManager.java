package com.dianping.cat.report.alert.sender.sender;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.alert.sender.AlertChannel;
import com.dianping.cat.report.alert.sender.AlertMessageEntity;

public class SenderManager extends ContainerHolder implements Initializable {

	@Inject
	private ServerConfigManager m_configManager;

	private Map<String, Sender> m_senders = new HashMap<String, Sender>();

	@Override
	public void initialize() throws InitializationException {
		m_senders = lookupMap(Sender.class);
	}

	public boolean sendAlert(AlertChannel channel, AlertMessageEntity message) {
		String channelName = channel.getName();

		try {
			Sender sender = m_senders.get(channelName);
			boolean result = true;
			String str = "nosend";

			if (m_configManager.isSendMachine()) {
				result = sender.send(message);
				str = String.valueOf(result);
			}

			String type = message.getType();

			Cat.logEvent("Channel:" + channelName, type + ":" + str, Event.SUCCESS, null);
			return result;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

}
