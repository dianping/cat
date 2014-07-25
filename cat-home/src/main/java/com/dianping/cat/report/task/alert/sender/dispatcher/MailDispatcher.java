package com.dianping.cat.report.task.alert.sender.dispatcher;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.system.tool.MailSMS;

public class MailDispatcher implements Dispatcher, LogEnabled {

	@Inject
	private MailSMS m_mailSms;

	public static final String ID = "mail";

	private Logger m_logger;

	@Override
	public boolean send(AlertMessageEntity message, String type) {
		try {
			m_mailSms.sendEmail(message.getTitle(), message.getContent(), message.getReceivers());

			String messageStr = message.toString();
			Cat.logEvent("AlertMail", type, Event.SUCCESS, messageStr);
			m_logger.info("AlertMail " + messageStr);
			return true;
		} catch (Exception ex) {
			Cat.logError("send mail error " + message.toString(), ex);
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
