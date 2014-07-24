package com.dianping.cat.report.task.alert.sender.dispatcher;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.system.tool.MailSMS;

public class MailDispatcher implements Dispatcher {

	@Inject
	private MailSMS m_mailSms;

	public static final String ID = AlertChannel.MAIL.getName();

	@Override
	public boolean send(AlertMessageEntity message) {
		try {
			m_mailSms.sendEmail(message.getTitle(), message.getContent(), message.getReceivers());
			Cat.logEvent("SendMail", message.toString());
			return true;
		} catch (Exception ex) {
			Cat.logError("send mail error " + message.toString(), ex);
			return false;
		}
	}

}
