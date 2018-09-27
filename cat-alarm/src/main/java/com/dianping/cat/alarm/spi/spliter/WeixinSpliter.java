package com.dianping.cat.alarm.spi.spliter;

import java.util.regex.Pattern;

import com.dianping.cat.alarm.spi.AlertChannel;

public class WeixinSpliter implements Spliter {

	public static final String ID = AlertChannel.WEIXIN.getName();

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String process(String content) {
		String weixinContent = content.replaceAll("<br/>", "\n");
		weixinContent = Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(weixinContent).replaceAll("");
		weixinContent = Pattern.compile("<table.*(?=</table>)</table>", Pattern.DOTALL).matcher(weixinContent)
		      .replaceAll("");

		return weixinContent;
	}

}
