package com.dianping.cat.message.consumer.impl;

import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.site.lookup.ContainerHolder;

public class DefaultAnalyzerFactoryImpl extends ContainerHolder implements
		AnalyzerFactory {

	@Override
	public MessageAnalyzer create(String name, long start, long duration,
			String domain, long extraTime) {
		if (name.equals("failure")) {
			FailureReportAnalyzer analyzer = lookup(FailureReportAnalyzer.class);
			
			analyzer.setAnalyzerInfo(start, duration, domain, extraTime);
			return analyzer;
		} else if (name.equals("transaction")) {

		}

		return null;
	}

	@Override
	public void release(Object component) {
		release(component);
	}
}
