package com.dianping.cat.alarm.spi.sender.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Java 邮件发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class JavaMailSender {

	private String username;
	private String password;
	private Properties properties;

	public JavaMailSender(String host, int port, String username, String password) {
		this.username = username;
		this.password = password;
		properties = new Properties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
	}

	public void sendEmail(String to, String subject, String content) {
		Session session = Session.getInstance(properties,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress("CAT <" + username + ">"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(MimeUtility.encodeText(subject, MimeUtility.mimeCharset("UTF-8"), null));

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, "text/html;charset=UTF-8");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);

			Transport.send(message);
		} catch (MessagingException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}

