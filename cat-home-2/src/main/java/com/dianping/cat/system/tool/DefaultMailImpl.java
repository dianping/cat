package com.dianping.cat.system.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.Security;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.message.Event;

public class DefaultMailImpl implements MailSMS, Initializable, LogEnabled {

	private BlockingQueue<Item> m_items = new LinkedBlockingDeque<Item>();

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

	public String getAddress() {
		return m_name;
	}

	@Override
	public void initialize() {
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		m_name = m_manager.getEmailAccount();
		m_password = m_manager.getEmailPassword();
		m_authenticator = new DefaultAuthenticator(m_name, m_password);

		Threads.forGroup("Cat").start(new MailSender());
	}

	public boolean sendEmail(String title, String content, List<String> emails) {
		Item item = new Item(title, content, emails);

		return m_items.offer(item);
	}

	public boolean sendEmailByGmail(String title, String content, List<String> emails) {
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
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
	}

	public boolean sendEmailInternal(String title, String content, List<String> emails) {
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

				conn.setDoOutput(true);
				conn.setDoInput(true);
				writer = new OutputStreamWriter(conn.getOutputStream());

				writer.write("&value=" + value);
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

	@Override
	public boolean sendSms(String title, String content, List<String> phones) {
		StringBuilder sb = new StringBuilder();
		
		for (String phone : phones) {
			InputStream in = null;
			try {
				String format = "http://10.1.1.84/sms/send/json?jsonm={type:808,mobile:\"%s\",pair:{body=\"%s\"}}";
				String urlAddress = String.format(format, phone, URLEncoder.encode(title, "utf-8"));
				URL url = new URL(urlAddress);
				URLConnection conn = url.openConnection();

				in = conn.getInputStream();
				sb.append(Files.forIO().readFrom(in, "utf-8")).append("");
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			} finally {
				try {
					if (in != null) {
						in.close();
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

	public static class Item {
		private String m_title;

		private String m_content;

		private List<String> m_emails;

		public Item(String title, String content, List<String> emails) {
			m_title = title;
			m_content = content;
			m_emails = emails;
		}

		public String getContent() {
			return m_content;
		}

		public List<String> getEmails() {
			return m_emails;
		}

		public String getTitle() {
			return m_title;
		}
	}

	private class MailSender implements Task {

		@Override
		public String getName() {
			return "send-mail";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					Item item = m_items.poll(5, TimeUnit.MILLISECONDS);

					if (item != null) {
						String title = item.getTitle();
						String content = item.getContent();
						List<String> emails = item.getEmails();

						boolean result = sendEmailInternal(title, content, emails);

						if (!result) {
							Cat.logEvent("InternalEmailSendError", title, Event.SUCCESS, content);
							boolean gmail = sendEmailByGmail(title, content, emails);

							if (gmail == false) {
								m_logger.error("Error when send email, title" + title + " receive:" + emails);

								Cat.logEvent("Email", title, Event.SUCCESS, content);
							}
						}
					}
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					m_logger.error(e.getMessage(), e);
				}
			}
		}

		@Override
		public void shutdown() {
		}

	}
}
