package com.dianping.cat.report.task.alert.sender.spliter;

import com.dianping.cat.report.task.alert.AlertConstants;

public class WeixinSpliter implements Spliter {

	public static final String ID = AlertConstants.WEIXIN;

	@Override
	public String process(String content) {
		return content.replaceAll("<br/>", "\n");
	}

	@Override
	public String getID() {
		return ID;
	}

}
