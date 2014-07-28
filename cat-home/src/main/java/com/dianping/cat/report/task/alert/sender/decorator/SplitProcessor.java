package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertChannel;

public interface SplitProcessor {

	public String process(String content, AlertChannel channel);
	
}
