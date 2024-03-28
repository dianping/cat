package com.dianping.cat.alarm.spi.spliter;

import com.dianping.cat.alarm.spi.AlertChannel;

/**
 * Jira Software 内容切割
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class JiraSpliter implements Spliter {

	public static final String ID = AlertChannel.JIRA.getName();

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String process(String content) {
		return content;
	}
}
