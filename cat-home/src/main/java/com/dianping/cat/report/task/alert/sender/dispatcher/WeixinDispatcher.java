package com.dianping.cat.report.task.alert.sender.dispatcher;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.system.tool.MailSMS;

public class WeixinDispatcher implements Dispatcher, LogEnabled {

	@Inject
	private MailSMS m_mailSms;

	public static final String ID = "weixin";

	private Logger m_logger;

	@Override
	public boolean send(AlertMessageEntity message, String type) {
		try {
			m_mailSms
			      .sendWeiXin(message.getTitle(), message.getContent(), message.getGroup(), message.getReceiverString());

			String messageStr = message.toString();
			Cat.logEvent("AlertWeiixin", type, Event.SUCCESS, messageStr);
			m_logger.info("AlertWeiixin " + messageStr);
			return true;
		} catch (Exception ex) {
			Cat.logError("send weixin error " + message.toString(), ex);
			return false;
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
