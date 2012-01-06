package com.dianping.cat.message.consumer.impl;

import com.dianping.cat.message.spi.MessageAnalyzer;

public interface AnalyzerFactory {

	public abstract MessageAnalyzer create(String name, long start,
			long duration, String domain, long extraTime);

}