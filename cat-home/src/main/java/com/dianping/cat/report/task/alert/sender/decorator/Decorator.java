package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public interface Decorator {

	String getId();

	String generateTitle(AlertEntity alert);

	String generateContent(AlertEntity alert);

}
