package com.dianping.cat.report.task.alert.sender.decorator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public abstract class Decorator {

	protected DateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public abstract String getId();

	public abstract String generateTitle(AlertEntity alert);

	public String generateContent(AlertEntity alert) {
		return alert.getContent();
	}

}
