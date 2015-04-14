package com.dianping.cat.report.alert.sender.spliter;

public interface Spliter {

	public String process(String content);

	public String getID();

}
