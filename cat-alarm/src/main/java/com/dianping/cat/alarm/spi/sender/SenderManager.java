package com.dianping.cat.alarm.spi.sender;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Event;

@Named
public class SenderManager extends ContainerHolder implements Initializable {

	@Inject
	private ServerConfigManager m_configManager;

	private Map<String, Sender> m_senders = new HashMap<String, Sender>();

	@Override
	public void initialize() throws InitializationException {
		m_senders = lookupMap(Sender.class);
	}

	public boolean sendAlert(AlertChannel channel, SendMessageEntity message) {
		String channelName = channel.getName();

		try {
			boolean result = false;
			String str = "nosend";

			if (m_configManager.isSendMachine()) {
				Sender sender = m_senders.get(channelName);

				result = sender.send(message);
				str = String.valueOf(result);
			}
			Cat.logEvent("Channel:" + channel, message.getType() + ":" + str, Event.SUCCESS, null);
			return result;
		} catch (Exception e) {
			Cat.logError("Channel [" + channel + "] " + message.toString(), e);
			return false;
		}
	}

}
