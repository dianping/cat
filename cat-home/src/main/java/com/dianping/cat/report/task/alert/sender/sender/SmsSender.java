package com.dianping.cat.report.task.alert.sender.sender;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class SmsSender implements Sender, LogEnabled {

	public static final String ID = AlertChannel.SMS.getName();
	
	private Logger m_logger;

	@Override
	public boolean send(AlertMessageEntity message, String type) {
		try {
			String messageStr = message.toString();

			if (!sendSms(message)) {
				Cat.logEvent("AlertSmsError", type, Event.SUCCESS, messageStr);
				m_logger.info("AlertSmsError " + messageStr);
				return false;
			}

			Cat.logEvent("AlertSms", type, Event.SUCCESS, messageStr);
			m_logger.info("AlertSms " + messageStr);
			return true;
		} catch (Exception ex) {
			Cat.logError("send sms error " + message.toString(), ex);
			return false;
		}
	}

	private boolean sendSms(AlertMessageEntity message) {
		String content = message.getTitle() + " " + message.getContent();
		List<String> phones = message.getReceivers();
		StringBuilder sb = new StringBuilder();

		for (String phone : phones) {
			InputStream in = null;
			try {
				String format = "http://10.1.1.84/sms/send/json?jsonm={type:808,mobile:\"%s\",pair:{body=\"%s\"}}";
				String urlAddress = String.format(format, phone, URLEncoder.encode(content, "utf-8"));
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

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
