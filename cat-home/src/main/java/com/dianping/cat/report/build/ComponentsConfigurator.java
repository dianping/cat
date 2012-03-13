package com.dianping.cat.report.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.report.ServerConfig;
import com.dianping.cat.report.graph.DefaultGraphBuilder;
import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.graph.ValueTranslater;
import com.dianping.cat.report.page.ip.IpManager;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		if (isEnv("dev") || property("env", null) == null) {
			all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
			      .req(MessageConsumer.class, new String[] { "realtime"/*, "dump-to-html"*/ }, "m_consumers"));
		} else {
			all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
			      .req(MessageConsumer.class, new String[] { "realtime" }, "m_consumers"));
		}

		all.add(C(ServerConfig.class)//
		      .config(E("consumerServers").value("127.0.0.1:2281"))//
		      .config(E("fileServer").value("127.0.0.1")));

		all.add(C(IpManager.class));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));
		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
		      .req(ValueTranslater.class));

		all.addAll(new ServiceComponentConfigurator().defineComponents());

		// Please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
