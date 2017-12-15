package com.dianping.cat.report.alert.sender.sender;

import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
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
			boolean result = true;
			String str = "nosend";

			if (m_configManager.isSendMachine()) {
				Sender sender = m_senders.get(channelName);

				result = sender.send(message);
				str = String.valueOf(result);
			}
			Cat.logEvent("Channel:" + channelName, message.getType() + ":" + str, Event.SUCCESS, null);
			return result;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

}
