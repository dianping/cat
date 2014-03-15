package com.dianping.cat.system.tool;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;
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

public class DefaultMailImpl implements MailSMS, Initializable, LogEnabled {

	@Inject
	private ServerConfigManager m_manager;

	private String m_name;

	private String m_password;

	private Authenticator m_authenticator;

	private boolean m_emailEnabled = false;

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

	public String getAddress() {
		return m_name;
	}

	@Override
	public void initialize() {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		m_name = m_manager.getEmailAccount();
		m_password = m_manager.getEmailPassword();
		m_authenticator = new DefaultAuthenticator(m_name, m_password);
		m_emailEnabled = true;
	}

	public boolean sendEmailByGmail(String title, String content, List<String> emails) {
		if (m_emailEnabled) {
			try {
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
			} catch (EmailException e) {
				Cat.logError(e);
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean sendEmail(String title, String content, List<String> emails) {
		for (String email : emails) {
			try {
				title = title.replaceAll(",", " ");
				content = content.replaceAll(",", " ");

				String value = title + "," + content;
				URL url = new URL("http://10.1.1.51/mail.v?type=1500&key=title,body&re=yong.you@dianping.com&to=" + email);
				URLConnection conn = url.openConnection();

				conn.setDoOutput(true);
				OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

				writer.write("&value=" + value);
				writer.flush();

				InputStream in = conn.getInputStream();
				String result = Files.forIO().readFrom(in, "utf-8");

				m_logger.info(title + " " + result);
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			}
		}
		return true;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
