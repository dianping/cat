package com.dianping.dog.alarm.rule.store;

import java.util.List;

public interface Storage<T> {
	
	boolean init(int period);

	void add(T data);

	List<T> getDataList();
}
