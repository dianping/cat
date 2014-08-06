package com.dianping.cat.report.task.alert.sender.decorator;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.Constants;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class FrontEndExceptionDecorator extends ExceptionDecorator {

	public static final String ID = AlertType.FRONT_END_EXCEPTION;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[CAT异常告警] [项目: ").append(Constants.FRONT_END).append("]");
		return sb.toString();
	}

	@Override
	protected Map<Object, Object> generateExceptionMap(AlertEntity alert) {
		String domain = Constants.FRONT_END;
		String contactInfo = buildContactInfo(domain);
		Map<Object, Object> map = new HashMap<Object, Object>();

		map.put("domain", domain);
		map.put("content", alert.getContent());
		map.put("date", m_dateFormat.format(alert.getDate()));
		map.put("contactInfo", contactInfo);

		return map;
	}

	@Override
	public String buildContactInfo(String domainName) {
		return "";
	}

}
