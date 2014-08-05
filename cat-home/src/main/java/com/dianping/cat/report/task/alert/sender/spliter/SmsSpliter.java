package com.dianping.cat.report.task.alert.sender.spliter;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public class SmsSpliter implements Spliter {

	public static final String ID = AlertChannel.SMS.getName();

	@Override
	public String process(String content) {
		return content.replaceAll("<br/>", " ");
	}

	@Override
	public String getID() {
		return ID;
	}

}
