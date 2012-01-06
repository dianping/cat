package com.dianping.cat.consumer.configuration;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.consumer.impl.AnalyzerFactory;
import com.dianping.cat.message.consumer.impl.DefaultAnalyzerFactoryImpl;
import com.dianping.cat.message.consumer.impl.DefaultMessageQueue;
import com.dianping.cat.message.consumer.impl.RealtimeConsumer;
import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzer.FailureHandler;
import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzer.Handler;
import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzer.LongUrlHandler;
import com.dianping.cat.message.consumer.model.failure.FailureReportAnalyzerConfig;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageQueue;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageQueue.class, DefaultMessageQueue.class).is(PER_LOOKUP));

		all.add(C(MessageConsumer.class, "realtime", RealtimeConsumer.class) //
				.config(E("consumerId").value("realtime") //
						, E("domain").value("Review") //
						, E("extraTime").value("300000")//
						, E("analyzerNames").value("failure-report") //
				));

		all.add(C(AnalyzerFactory.class, DefaultAnalyzerFactoryImpl.class));

		String handlers = "failure,long-url";
		String failureTypes = "Error,RuntimeException,Exception";

		all.add(C(Handler.class, "failure", FailureHandler.class)//
				.config(E("failureType").value(failureTypes)).is(PER_LOOKUP));
		all.add(C(Handler.class, "long-url", LongUrlHandler.class) //
				.config(E("threshold").value("2000")).is(PER_LOOKUP));

		all.add(C(FailureReportAnalyzerConfig.class, "failure-analyzer-config",
				FailureReportAnalyzerConfig.class)//
				.config(E("handlers").value(handlers)//
						, E("machines").value("All")));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
