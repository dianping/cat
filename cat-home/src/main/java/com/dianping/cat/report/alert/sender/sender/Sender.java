package com.dianping.cat.report.alert.sender.sender;

import com.dianping.cat.report.alert.sender.AlertMessageEntity;

public interface Sender {

	public String getId();

	public boolean send(AlertMessageEntity message);

}
