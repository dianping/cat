package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.DefaultAnalyzerFactory;
import com.dianping.cat.consumer.DefaultMessageQueue;
import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer.FailureHandler;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer.Handler;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer.LongUrlHandler;
import com.dianping.cat.consumer.ip.IpAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MessageQueue.class, DefaultMessageQueue.class).is(PER_LOOKUP));

		all.add(C(AnalyzerFactory.class, DefaultAnalyzerFactory.class));

		all.add(C(MessageConsumer.class, "realtime", RealtimeConsumer.class) //
		      .req(AnalyzerFactory.class).config(E("consumerId").value("realtime") //
		            , E("extraTime").value(property("extraTime", "300000"))//
		            , E("analyzerNames").value("failure,transaction,ip")));

		String failureTypes = "Error,RuntimeException,Exception";

		all.add(C(Handler.class, "failure-handler", FailureHandler.class)//
		      .config(E("failureType").value(failureTypes))//
		      .req(MessageStorage.class, "html"));

		all.add(C(Handler.class, "long-url-handler", LongUrlHandler.class) //
		      .config(E("threshold").value("2000"))//
		      .req(MessageStorage.class, "html"));

		all.add(C(FailureReportAnalyzer.class).is(PER_LOOKUP) //
		      .config(E("reportPath").value("target/report/failure/")) //
		      .req(MessageManager.class) //
		      .req(Handler.class, new String[] { "failure-handler", "long-url-handler" }, "m_handlers"));

		all.add(C(TransactionReportAnalyzer.class).is(PER_LOOKUP) //
		      .req(MessageManager.class) //
		      .req(MessageStorage.class, "html") //
		      .config(E("reportPath").value("target/report/transaction/")));

		all.add(C(TransactionAnalyzer.class).is(PER_LOOKUP) //
		      .req(BucketManager.class, MessagePathBuilder.class));

		all.add(C(IpAnalyzer.class).is(PER_LOOKUP));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
