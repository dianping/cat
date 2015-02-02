package com.dianping.cat.report.task.alert.sender.spliter;

public interface Spliter {

	public String process(String content);

	public String getID();

}
