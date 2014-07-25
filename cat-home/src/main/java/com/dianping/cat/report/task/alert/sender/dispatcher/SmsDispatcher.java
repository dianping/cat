package com.dianping.cat.report.task.alert.sender.dispatcher;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.system.tool.MailSMS;

public class SmsDispatcher implements Dispatcher {

	@Inject
	private MailSMS m_mailSms;

	public static final String ID = "sms";

	@Override
	public boolean send(AlertMessageEntity message) {
		try {
			m_mailSms.sendSms(message.getTitle(), message.getContent(), message.getReceivers());
			Cat.logEvent("SendSms", message.toString());
			return true;
		} catch (Exception ex) {
			Cat.logError("send sms error " + message.toString(), ex);
			return false;
		}
	}

	@Override
	public String getId() {
		return ID;
	}

}
