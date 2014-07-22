package com.dianping.cat.report.task.alert.sender;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.system.tool.MailSMS;

public abstract class BaseSender implements LogEnabled {

	protected Logger m_logger;

	@Inject
	protected MailSMS m_mailSms;

	protected abstract void sendLog(String title, String content, List<String> receivers);

	public abstract boolean sendAlert(List<String> receivers, String domain, String title, String content,
	      String alertType);

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
