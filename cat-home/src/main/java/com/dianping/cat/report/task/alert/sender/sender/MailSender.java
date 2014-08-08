package com.dianping.cat.report.task.alert.sender.sender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import javax.mail.Authenticator;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class MailSender implements Initializable, Sender, LogEnabled {

	public static final String ID = AlertChannel.MAIL.getName();

	@Inject
	private ServerConfigManager m_manager;

	private String m_name;

	private String m_password;

	private Authenticator m_authenticator;

	private Logger m_logger;

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
	public void enableLogging(Logger logger) {
		m_logger = logger;
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
	public boolean send(AlertMessageEntity message, String type) {
		boolean result = sendEmail(message);

		if (!result) {
			Cat.logEvent("InternalMailSender", "error", Event.SUCCESS, null);

			boolean gmail = sendEmailByGmail(message);

			if (gmail == false) {
				return false;
			}
		}
		return true;
	}

	private boolean sendEmail(AlertMessageEntity message) {
		String title = message.getTitle();
		String content = message.getContent();
		List<String> emails = message.getReceivers();
		StringBuilder sb = new StringBuilder();

		for (String email : emails) {
			InputStream in = null;
			OutputStreamWriter writer = null;
			try {
				title = title.replaceAll(",", " ");
				content = content.replaceAll(",", " ");

				String value = title + "," + content;
				URL url = new URL("http://10.1.1.51/mail.v?type=1500&key=title,body&re=yong.you@dianping.com&to=" + email);
				URLConnection conn = url.openConnection();

				conn.setConnectTimeout(2000);
				conn.setReadTimeout(3000);
				conn.setDoOutput(true);
				conn.setDoInput(true);
				writer = new OutputStreamWriter(conn.getOutputStream());
				String encode = "&value=" + URLEncoder.encode(value, "utf-8");

				writer.write(encode);
				writer.flush();

				in = conn.getInputStream();
				String result = Files.forIO().readFrom(in, "utf-8");

				sb.append(result).append("");
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (writer != null) {
						writer.close();
					}
				} catch (IOException e) {
				}
			}
		}
		if (sb.indexOf("200") > -1) {
			return true;
		} else {
			return false;
		}
	}

	private boolean sendEmailByGmail(AlertMessageEntity message) {
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
