package com.dianping.cat.consumer.configuration;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.consumer.failure.FailureAnalyzer;
import com.dianping.cat.message.consumer.failure.FailureAnalyzer.ErrorHandler;
import com.dianping.cat.message.consumer.failure.FailureAnalyzer.ExceptionHandler;
import com.dianping.cat.message.consumer.failure.FailureAnalyzer.Handler;
import com.dianping.cat.message.consumer.failure.FailureAnalyzer.LongUrlHandler;
import com.dianping.cat.message.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.message.consumer.impl.DefaultMessageQueue;
import com.dianping.cat.message.consumer.impl.RealtimeConsumer;
import com.dianping.cat.message.spi.MessageAnalyzer;
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
						, E("analyzerNames").value("failure-report") //
				));

		all.add(C(MessageAnalyzer.class, "failure-report",
				FailureReportAnalyzer.class) //
				.is(PER_LOOKUP));

		String[] handlerNames = new String[] { "error", "exception", "long-url" };

		all.add(C(Handler.class, "error", ErrorHandler.class));
		all.add(C(Handler.class, "exception", ExceptionHandler.class));
		all.add(C(Handler.class, "long-url", LongUrlHandler.class) //
				.config(E("threshold").value("2000")));

		all.add(C(MessageAnalyzer.class, "failure", FailureAnalyzer.class) //
				.is(PER_LOOKUP) //
				.req(Handler.class, handlerNames, "m_handlers"));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
