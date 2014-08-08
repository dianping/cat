package com.dianping.cat.report.task.alert.sender.sender;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.ContainerHolder;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class SenderManager extends ContainerHolder implements Initializable, LogEnabled {

	private Map<String, Sender> m_senders = new HashMap<String, Sender>();

	private Logger m_logger;

	@Override
	public void initialize() throws InitializationException {
		m_senders = lookupMap(Sender.class);
	}

	public boolean sendAlert(AlertChannel channel, AlertMessageEntity message) {
		String channelName = channel.getName();

		try {
			Sender sender = m_senders.get(channelName);
			boolean result = sender.send(message);
			String type = message.getType();

			if (result) {
				Cat.logEvent("Channel:" + channelName, type + ":success", Event.SUCCESS, null);
			} else {
				Cat.logEvent("Channel:" + channelName, type + ":fail", Event.SUCCESS, null);
			}
			m_logger.info("Alert Channel " + channelName + ",content" + message.toString());
			return result;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
