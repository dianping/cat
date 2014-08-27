package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public interface Decorator {

	public String getId();

	public String generateTitle(AlertEntity alert);

	public String generateContent(AlertEntity alert);
	
	public String buildContactInfo(String group);

}
