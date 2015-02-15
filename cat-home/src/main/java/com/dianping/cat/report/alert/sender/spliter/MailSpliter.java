package com.dianping.cat.report.alert.sender.spliter;

import com.dianping.cat.report.alert.sender.AlertChannel;

public class MailSpliter implements Spliter {

	public static final String ID = AlertChannel.MAIL.getName();

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String process(String content) {
		return content;
	}

}
