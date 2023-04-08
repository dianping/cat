package com.dianping.cat.alarm.spi.spliter;

import com.dianping.cat.alarm.spi.AlertChannel;

/**
 * 飞书内容切割
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class FeishuSpliter implements Spliter {

	public static final String ID = AlertChannel.DINGTALK.getName();

	@Override
	public String process(String content) {
		if (content.length() > 2000) {
			content = content.substring(0, 2000) + "...";
		}
		return content;
	}

	@Override
	public String getID() {
		return ID;
	}
}
