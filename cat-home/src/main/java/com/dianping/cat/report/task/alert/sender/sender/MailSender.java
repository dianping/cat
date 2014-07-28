package com.dianping.cat.report.task.alert.sender.sender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class MailSender implements Sender, LogEnabled {

	public static final String ID = "mail";

	private Logger m_logger;

	@Override
	public boolean send(AlertMessageEntity message, String type) {
		try {
			String messageStr = message.toString();

			if (!sendEmail(message)) {
				Cat.logEvent("AlertMailError", type, Event.SUCCESS, messageStr);
				m_logger.info("AlertMailError " + messageStr);
				return false;
			}

			Cat.logEvent("AlertMail", type, Event.SUCCESS, messageStr);
			m_logger.info("AlertMail " + messageStr);
			return true;
		} catch (Exception ex) {
			Cat.logError("send mail error " + message.toString(), ex);
			return false;
		}
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
	public String getId() {
		return ID;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
