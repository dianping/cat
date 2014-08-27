package com.dianping.cat.report.task.alert.sender.spliter;

import java.util.regex.Pattern;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public class WeixinSpliter implements Spliter {

	public static final String ID = AlertChannel.WEIXIN.getName();

	@Override
	public String process(String content) {
		String weixinContent = content.replaceAll("<br/>", "\n");
		return Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(weixinContent).replaceAll("");
	}

	@Override
	public String getID() {
		return ID;
	}

}
