package com.dianping.cat.report.task.alert.sender.sender;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class SenderManager implements Initializable {

	@Inject(type = Sender.class, value = MailSender.ID)
	protected Sender m_mailSender;

	@Inject(type = Sender.class, value = WeixinSender.ID)
	protected Sender m_weixinSender;

	@Inject(type = Sender.class, value = SmsSender.ID)
	protected Sender m_smsSender;

	private Map<String, Sender> m_senders = new HashMap<String, Sender>();

	@Override
	public void initialize() throws InitializationException {
		m_senders.put(m_mailSender.getId(), m_mailSender);
		m_senders.put(m_weixinSender.getId(), m_weixinSender);
		m_senders.put(m_smsSender.getId(), m_smsSender);
	}

	public boolean sendAlert(String channelName, String type, AlertMessageEntity message) {
		Sender sender = m_senders.get(channelName);
		return sender.send(message, type);
	}

	public void setMailSender(Sender sender) {
		m_mailSender = sender;
	}

	public void setSmsSender(Sender sender) {
		m_smsSender = sender;
	}

	public void setWeixinSender(Sender sender) {
		m_weixinSender = sender;
	}

}
