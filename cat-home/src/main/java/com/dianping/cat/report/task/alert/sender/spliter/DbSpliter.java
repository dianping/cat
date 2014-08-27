package com.dianping.cat.report.task.alert.sender.spliter;

import java.util.regex.Pattern;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public class DbSpliter implements Spliter {

	public static final String ID = AlertChannel.DATABASE.getName();

	@Override
	public String process(String content) {
		return Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(content).replaceAll("");
	}

	@Override
	public String getID() {
		return ID;
	}

}
