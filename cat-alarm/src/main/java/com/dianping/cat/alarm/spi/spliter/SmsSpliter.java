package com.dianping.cat.alarm.spi.spliter;

import java.util.regex.Pattern;

import com.dianping.cat.alarm.spi.AlertChannel;

public class SmsSpliter implements Spliter {

	public static final String ID = AlertChannel.SMS.getName();

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String process(String content) {
		String smsContent = content.replaceAll("<br/>", " ");
		return Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(smsContent).replaceAll("");
	}

}
