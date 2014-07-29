package com.dianping.cat.report.task.alert.sender.spliter;

import com.dianping.cat.report.task.alert.sender.AlertConstants;

public class MailSpliter implements Spliter {

	public static final String ID = AlertConstants.MAIL;

	@Override
	public String process(String content) {
		return content;
	}

	@Override
	public String getID() {
		return ID;
	}

}
