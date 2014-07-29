package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.AlertConstants;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class ExceptionDecorator extends DefaultDecorator {

	public static final String ID = AlertConstants.EXCEPTION;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[CAT异常告警] [项目: ").append(alert.getGroup()).append("]");
		return sb.toString();
	}

	@Override
	public String generateContent(AlertEntity alert) {
		try {
			StringBuilder sb = new StringBuilder();
			String domain = alert.getGroup();
			String date = m_format.format(alert.getDate());

			sb.append("[CAT异常告警] [项目: ").append(domain).append("] : ");
			sb.append(alert.getContent()).append("[时间: ").append(date).append("]");
			sb.append(" <a href='").append("http://cat.dianpingoa.com/cat/r/p?domain=").append(domain).append("&date=")
			      .append(date).append("'>点击此处查看详情[联系人修改请联系黄永,修改CMDB]</a>").append("<br/>");
			sb.append(buildContactInfo(domain));

			return sb.toString();
		} catch (Exception ex) {
			Cat.logError("build exception content error:" + alert.toString(), ex);
			return null;
		}
	}

}
