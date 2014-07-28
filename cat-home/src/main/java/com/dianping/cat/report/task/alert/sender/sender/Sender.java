package com.dianping.cat.report.task.alert.sender.sender;

import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public interface Sender {

	public String getId();

	public boolean send(AlertMessageEntity message, String alertType);

}
