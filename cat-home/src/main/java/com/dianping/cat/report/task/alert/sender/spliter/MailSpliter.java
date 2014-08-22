package com.dianping.cat.report.task.alert.sender.spliter;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public class MailSpliter implements Spliter {

	public static final String ID = AlertChannel.MAIL.getName();

	@Override
	public String process(String content) {
		return content+"<br/><a href=\"http://web.cmdb.dp/app-alter/app\">修改项目信息请点击</a>";
	}

	@Override
	public String getID() {
		return ID;
	}

}
