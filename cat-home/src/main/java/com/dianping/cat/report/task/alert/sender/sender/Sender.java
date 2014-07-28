package com.dianping.cat.report.task.alert.sender.sender;

import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public interface Sender {

	String getId();

	boolean send(AlertMessageEntity message, String type);

}
