package com.dianping.cat.consumer;

import org.unidal.lookup.ContainerHolder;


public class DefaultMessageAnalyzerFactory extends ContainerHolder implements MessageAnalyzerFactory {
	@Override
	public MessageAnalyzer create(String name, long startTime, long duration, long extraTime) {
		MessageAnalyzer analyzer = lookup(MessageAnalyzer.class, name);

		analyzer.setAnalyzerInfo(startTime, duration, extraTime);

		return analyzer;
	}
}
