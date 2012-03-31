package com.dianping.cat.report.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.job.DumpToHdfsConsumer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.report.graph.DefaultGraphBuilder;
import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.graph.ValueTranslater;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		String defaultLocalMode = "true";

		if ("true".equals(defaultLocalMode)) {
			all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
			      .req(MessageConsumer.class, new String[] { "realtime" }, "m_consumers"));
		} else {
			all.add(C(MessageConsumerRegistry.class, DefaultMessageConsumerRegistry.class) //
			      .req(MessageConsumer.class, new String[] { "realtime", DumpToHdfsConsumer.ID }, "m_consumers"));
		}

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
