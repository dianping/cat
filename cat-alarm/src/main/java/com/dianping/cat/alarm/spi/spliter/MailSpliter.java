package com.dianping.cat.alarm.spi.spliter;

import com.dianping.cat.alarm.spi.AlertChannel;

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
