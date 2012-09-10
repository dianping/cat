package com.dianping.dog.alarm.rule;

public interface DataVistor<T,R> {
	void visit(T rule);

	R getResult();
}
