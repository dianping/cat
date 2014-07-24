package com.dianping.cat.report.task.alert.sender.dispatcher;

import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public interface Dispatcher {

	boolean send(AlertMessageEntity message);

}
