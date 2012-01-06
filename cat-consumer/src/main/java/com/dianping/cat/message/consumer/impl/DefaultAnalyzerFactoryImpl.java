package com.dianping.cat.message.consumer.impl;

import java.util.List;

import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzer;
import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzer.Handler;
import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzerConfig;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.site.helper.Splitters;
import com.site.lookup.ContainerHolder;

public class DefaultAnalyzerFactoryImpl extends ContainerHolder implements
		AnalyzerFactory {

	@Override
	public MessageAnalyzer create(String name, long start, long duration,
			String domain, long extraTime) {
		if (name.equals("failure")) {
			FailureReportAnalyzer analyzer = new FailureReportAnalyzer(start,
					duration, domain, extraTime);

			FailureReportAnalyzerConfig config =lookup(FailureReportAnalyzerConfig.class,"failure-analyzer-config");

			String handlers = config.getHandlers();
			List<String> handlerList = Splitters.by(",").noEmptyItem().split(handlers);
			String machines = config.getMachines();
			
			for (String str : handlerList) {
				if (str.equals("failure")) {
					analyzer.addHandlers(lookup(Handler.class, "failure"));
				} else if (str.equals("long-url")) {
					analyzer.addHandlers(lookup(Handler.class, "long-url"));
				}
			}
			analyzer.setMachines(machines);
			return analyzer;
		} else if (name.equals("transaction")) {

		}

		return null;
	}
}
