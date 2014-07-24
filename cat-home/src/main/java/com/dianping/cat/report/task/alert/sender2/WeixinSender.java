package com.dianping.cat.report.task.alert.sender2;

import java.util.List;

import com.dianping.cat.Cat;

public class WeixinSender extends BaseSender {
	@Override
	protected void sendLog(String title, String content, List<String> receivers) {
		StringBuilder builder = new StringBuilder();

		builder.append(title).append(" ").append(content).append(" ");
		for (String receiver : receivers) {
			builder.append(receiver).append(" ");
		}

		Cat.logEvent("SendWeixin", builder.toString());
		m_logger.info("SendWeixin" + builder.toString());
	}

	@Override
	public boolean sendAlert(List<String> receivers, String domain, String title, String content) {
		try {
			content = content.replaceAll("<br/>", "\n");
			m_mailSms.sendWeiXin(title, content, domain, mergeList(receivers));
			sendLog(title, content, receivers);
			return true;
		} catch (Exception ex) {
			Cat.logError("send weixin error" + " " + title + " " + content, ex);
			return false;
		}
	}

	private String mergeList(List<String> receivers) {
		StringBuilder builder = new StringBuilder();

		for (String receiver : receivers) {
			builder.append(receiver).append(",");
		}

		String tmpResult = builder.toString();
		if (tmpResult.endsWith(",")) {
			return tmpResult.substring(0, tmpResult.length() - 1);
		} else {
			return tmpResult;
		}
	}
}
