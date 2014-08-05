package com.dianping.cat.report.task.alert.sender.spliter;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public class MailSpliter implements Spliter {

	public static final String ID = AlertChannel.MAIL.getName();

	@Override
	public String process(String content) {
		return content;
	}

	@Override
	public String getID() {
		return ID;
	}

}
