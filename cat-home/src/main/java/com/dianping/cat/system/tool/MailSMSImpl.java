package com.dianping.cat.system.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.mailremote.remote.MailService;
import com.dianping.sms.biz.SMSService;
import com.dianping.sms.biz.SMSType;

public class MailSMSImpl implements MailSMS, Initializable, LogEnabled {

	private final static int DEFAULT_EMAIL_TYPE = 15;

	private final SMSType DEFAULT_MESSAGE_TYPE = SMSType.MONITOR;

	private boolean m_active = false;

	private Logger m_logger;

	private MailService m_mailService;

	@Inject
	private ServerConfigManager m_serverConfig;

	private SMSService m_smsService;

	private boolean m_initialize = false;;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() {
		if (m_serverConfig.isJobMachine() && !m_serverConfig.isLocalMode()) {
			try {
				ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/remoteService.xml");

				m_mailService = (MailService) ctx.getBean("mailService");
				m_smsService = (SMSService) ctx.getBean("smsService");
				m_initialize = true;
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	@Override
	public boolean sendEmail(String title, String content, List<String> emails) {
		if (!m_initialize) {
			initialize();
		}
		boolean sendResult = false;

		if (m_serverConfig.isJobMachine()) {
			if (emails.size() > 0) {
				for (String mail : emails) {
					try {
						m_mailService.send(DEFAULT_EMAIL_TYPE, mail, title, content);
						sendResult = true;
						m_logger.info("CAT send email to! " + mail + ",title:" + title);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			} else {
				m_logger.info("CAT email has no recevers ! Email title:" + title);
			}
		} else {
			throw new RuntimeException("CAT server config is disable!");
		}
		return sendResult;
	}

	@Override
	public boolean sendSMS(String content, List<String> phones) {
		if (!m_initialize) {
			initialize();
		}
		boolean sendResult = false;

		if (m_serverConfig.isJobMachine()) {
			if (phones != null && phones.size() > 0) {
				for (String phone : phones) {
					m_logger.info("CAT sms send to ! " + phone + " " + content);
					if (m_active) {
						try {
							Map<String, String> pair = new HashMap<String, String>();

							pair.put("content", content);
							m_smsService.send(DEFAULT_MESSAGE_TYPE, phone, pair);
							sendResult = true;
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				}
			} else {
				m_logger.info("CAT sms has no recevers ! " + content);
			}
		} else {
			throw new RuntimeException("CAT server config is disable!");
		}
		return sendResult;
	}

}
