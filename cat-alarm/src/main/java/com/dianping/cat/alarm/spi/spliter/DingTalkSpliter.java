package com.dianping.cat.alarm.spi.spliter;

import com.dianping.cat.alarm.spi.AlertChannel;

import java.util.regex.Pattern;

public class DingTalkSpliter implements Spliter {

	public static final String ID = AlertChannel.DINGDING.getName();

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String process(String content) {


		// 钉钉发送消息限制文本最长5000
		content = content.replaceAll(",", " ").replaceAll("<a href.*(?=</a>)</a>", "").replace("<br/>", "\n");
		content = Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(content).replaceAll("");
		content = Pattern.compile("<table.*(?=</table>)</table>", Pattern.DOTALL).matcher(content).replaceAll("");
		content = content.replace("\n\n", "");  // 去除连续回车
		if (content.length() > 2000) {
			content = content.substring(0, 2000) + "...";
		}

		return content;
	}

}
