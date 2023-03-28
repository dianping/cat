package com.dianping.cat.alarm.spi.spliter;

import com.dianping.cat.alarm.spi.AlertChannel;

/**
 * 企业微信内容切割
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class WeComSpliter implements Spliter {

	public static final String ID = AlertChannel.WECOM.getName();

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
