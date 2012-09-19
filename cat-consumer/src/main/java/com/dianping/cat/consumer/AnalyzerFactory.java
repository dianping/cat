package com.dianping.cat.consumer;

import com.dianping.cat.message.spi.MessageAnalyzer;

public interface AnalyzerFactory {

	public MessageAnalyzer create(String name, long start, long duration, long extraTime);

	public void release(Object component);

}