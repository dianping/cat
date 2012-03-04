package com.dianping.cat.report.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.report.ReportModule;
import com.dianping.cat.report.ServerConfig;
import com.dianping.cat.report.graph.DefaultGraphBuilder;
import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.graph.ValueTranslater;
import com.dianping.cat.report.page.failure.FailureManager;
import com.dianping.cat.report.page.ip.IpManager;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.CompositeTransactionModelService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionModelService;
import com.dianping.cat.report.page.model.transaction.RemoteTransactionModelService;
import com.dianping.cat.report.page.service.provider.FailureModelProvider;
import com.dianping.cat.report.page.service.provider.IpModelProvider;
import com.dianping.cat.report.page.service.provider.ModelProvider;
import com.dianping.cat.report.page.service.provider.TransactionModelProvider;
import com.dianping.cat.report.page.transaction.TransactionManager;
import com.site.lookup.configuration.Component;
import com.site.web.configuration.AbstractWebComponentsConfigurator;

public class ComponentsConfigurator extends AbstractWebComponentsConfigurator {
	@Override
	@SuppressWarnings("unchecked")
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		if (isEnv("dev") || property("env", null) == null) {
			all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
			      .req(MessageConsumer.class, new String[] { "realtime", "dump-to-html" }, "m_consumers"));
		} else {
			all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
			      .req(MessageConsumer.class, new String[] { "realtime" }, "m_consumers"));
		}

		all.add(C(ServerConfig.class)//
		      .config(E("consumerServers").value("127.0.0.1:2281"))//
		      .config(E("fileServer").value("127.0.0.1")));

		all.add(C(ModelProvider.class, "failure", FailureModelProvider.class).req(MessageConsumer.class, "realtime"));

		all.add(C(ModelProvider.class, "transaction", TransactionModelProvider.class).req(MessageConsumer.class,
		      "realtime"));

		all.add(C(ModelProvider.class, "ip", IpModelProvider.class).req(MessageConsumer.class, "realtime"));

		all.add(C(FailureManager.class));

		all.add(C(TransactionManager.class));

		all.add(C(IpManager.class));

		all.add(C(ModelService.class, "transaction-local", LocalTransactionModelService.class) //
		      .req(MessageConsumer.class, "realtime"));
		all.add(C(ModelService.class, "transaction-localhost", RemoteTransactionModelService.class) //
		      .config(E("host").value("localhost")));
		all.add(C(ModelService.class, "transaction", CompositeTransactionModelService.class) //
		      .req(ModelService.class, new String[] { "transaction-local" }, "m_services"));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));
		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
				.req(ValueTranslater.class));

		// Please keep it last
		defineModuleRegistry(all, ReportModule.class, ReportModule.class);

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
