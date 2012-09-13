package com.dianping.dog.alarm.rule.store;

import java.util.List;

public interface StoreQueue<T extends Data> {
	
	void addData(T data);

	List<T> getAll();

}
