package com.dianping.cat.consumer;

import org.unidal.lookup.ContainerHolder;

public class DefaultMessageAnalyzerManager extends ContainerHolder implements MessageAnalyzerManager {
	@Override
	public MessageAnalyzer getAnalyzer(String name, long startTime) {
		return null;
	}
}
