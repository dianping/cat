package com.dianping.cat.report.task.alert.sender.dispatcher;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.system.tool.MailSMS;

public class WeixinDispatcher implements Dispatcher {

	@Inject
	private MailSMS m_mailSms;

	public static final String ID = AlertChannel.WEIXIN.getName();

	@Override
	public boolean send(AlertMessageEntity message) {
		try {
			m_mailSms.sendWeiXin(message.getTitle(), message.getContent(), message.getGroup(),
			      message.getReceiverString());
			Cat.logEvent("SendWeixin", message.toString());
			return true;
		} catch (Exception ex) {
			Cat.logError("send weixin error " + message.toString(), ex);
			return false;
		}
	}

}
