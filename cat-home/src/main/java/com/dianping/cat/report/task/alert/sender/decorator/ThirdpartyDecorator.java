package com.dianping.cat.report.task.alert.sender.decorator;

import java.util.Date;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.sender.AlertConstants;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class ThirdpartyDecorator extends DefaultDecorator {

	public static final String ID = AlertConstants.THIRDPARTY;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[CAT第三方告警] [项目: ").append(alert.getGroup()).append("]");
		return sb.toString();
	}

	@Override
	public String generateContent(AlertEntity alert) {
		try {
			StringBuilder sb = new StringBuilder();
			String time = m_format.format(new Date());
			String group = alert.getGroup();

			sb.append("[CAT第三方告警] [项目: ").append(group).append("] : ");
			sb.append(alert.getContent()).append("[时间: ").append(time).append("]");
			sb.append("<br/>").append(buildContactInfo(group));

			return sb.toString();
		} catch (Exception ex) {
			Cat.logError("build third party content error:" + alert.toString(), ex);
			return null;
		}
	}

}
