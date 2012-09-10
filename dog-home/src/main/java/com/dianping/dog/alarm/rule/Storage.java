package com.dianping.dog.alarm.rule;

import com.dianping.dog.event.Event;

public interface Storage<T extends Event,R> {
	void init(long period);

	void save(T data,long currentTime);

	R getData(DataVistor<T,R> vistor);
}
