/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.alarm.spi.sender;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.util.JavaMailSender;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MailSender extends AbstractSender {

	public static final String ID = AlertChannel.MAIL.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(SendMessageEntity message) {
		Sender sender = querySender();
		boolean result = false;

		boolean batchSend = sender.isBatchSend();
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

	private boolean sendEmail(SendMessageEntity message, String receiver, Sender sender) {
		String title = message.getTitle();
		String content = message.getContent();
		content += "<br/><a href=\"" + message.getSettingsLink() + "\">\uD83D\uDD27 告警规则</a>";
		content += "  <a href=\"" + message.getViewLink() + "\">\uD83D\uDD14 查看告警</a>";
		String url = sender.getUrl();
		if (url.startsWith("http")) { // 保留原味
			String urlPrefix = sender.getUrl();
			String urlPars = m_senderConfigManager.queryParString(sender);
			String time = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

			try {
				urlPars = urlPars.replace("${receiver}", receiver)
					.replace("${title}", URLEncoder.encode(title, "utf-8"))
					.replace("${content}", URLEncoder.encode(content, "utf-8"))
					.replace("${time}", URLEncoder.encode(time, "utf-8"));

			} catch (Exception e) {
				Cat.logError(e);
			}
			return httpSend(sender.getSuccessCode(), sender.getType(), urlPrefix, urlPars);
		} else {
			String[] urls = url.split(":");
			String host = urls[0];
			int port = Integer.parseInt(urls[1]);
			String urlPars = m_senderConfigManager.queryParString(sender);
			Map<String, String> map = parseUrls(urlPars);
			String username = map.get("username");
			String password = map.get("password");
			JavaMailSender javaMailSender = new JavaMailSender(host, port, username, password);
			javaMailSender.sendEmail(receiver, title, content);
			return true;
		}
	}
}
