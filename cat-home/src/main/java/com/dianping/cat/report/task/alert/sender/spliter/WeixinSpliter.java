package com.dianping.cat.report.task.alert.sender.spliter;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public class WeixinSpliter implements Spliter {

	public static final String ID = AlertChannel.WEIXIN.getName();

	@Override
	public String process(String content) {
		return content.replaceAll("<br/>", "\n");
	}

	@Override
	public String getID() {
		return ID;
	}

}
