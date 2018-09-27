package com.dianping.cat.alarm.spi.spliter;

public interface Spliter {

	public String process(String content);

	public String getID();

}
