package com.dianping.cat.report.task.alert.sender.decorator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public abstract class DefaultDecorator implements Decorator {

	protected DateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public String generateContent(AlertEntity alert) {
		return alert.getContent() + buildContactInfo(alert.getGroup());
	}

}
