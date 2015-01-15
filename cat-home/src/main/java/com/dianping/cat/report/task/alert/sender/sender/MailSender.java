package com.dianping.cat.report.task.alert.sender.sender;

import java.net.URLEncoder;
import java.util.List;

import javax.mail.Authenticator;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class MailSender extends AbstractSender implements Initializable {

	public static final String ID = AlertChannel.MAIL.getName();

	@Inject
	private ServerConfigManager m_manager;

	private String m_name;

	private String m_password;

	private Authenticator m_authenticator;

	private HtmlEmail createHtmlEmail() throws EmailException {
		HtmlEmail email = new HtmlEmail();

		email.setHostName("smtp.gmail.com");
		email.setSmtpPort(465);
		email.setAuthenticator(m_authenticator);
		email.setSSL(true);
		email.setFrom(m_name);
		email.setCharset("utf-8");
		return email;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void initialize() {
		m_name = m_manager.getEmailAccount();
		m_password = m_manager.getEmailPassword();
		m_authenticator = new DefaultAuthenticator(m_name, m_password);
	}

	@Override
	public boolean send(AlertMessageEntity message) {
		com.dianping.cat.home.sender.entity.Sender sender = querySender();
		boolean batchSend = sender.isBatchSend();
		boolean result = false;

		if (batchSend) {
			String emails = message.getReceiverString();

			result = sendEmail(message, emails, sender);
		} else {
			List<String> emails = message.getReceivers();

			for (String email : emails) {
				boolean success = sendEmail(message, email, sender);
				result = result || success;
			}
		}
		return result;
	}

	private boolean sendEmail(AlertMessageEntity message, String emails,
	      com.dianping.cat.home.sender.entity.Sender sender) {
		String title = message.getTitle().replaceAll(",", " ");
		String content = message.getContent().replaceAll(",", " ");
		String urlPrefix = sender.getUrl();
		String urlPars = m_senderConfigManager.queryParString(sender);

		try {
			urlPars = urlPars.replace("${email}", URLEncoder.encode(emails, "utf-8"))
			      .replace("${title}", URLEncoder.encode(title, "utf-8"))
			      .replace("${content}", URLEncoder.encode(content, "utf-8"));

		} catch (Exception e) {
			Cat.logError(e);
		}

		return httpGetSend(sender.getSuccessCode(), urlPrefix, urlPars);
	}

	protected boolean sendEmailByGmail(AlertMessageEntity message) {
		try {
			String title = message.getTitle();
			String content = message.getContent();
			List<String> emails = message.getReceivers();
			HtmlEmail email = createHtmlEmail();

			email.setSubject(title);
			email.setFrom("CAT@dianping.com");

			if (content != null) {
				email.setHtmlMsg(content);
			}
			if (emails != null && emails.size() > 0) {
				for (String to : emails) {
					email.addTo(to);
				}
				email.send();
			}
			return true;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
	}

}
