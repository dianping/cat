package com.dianping.cat.report.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageConsumerRegistry;
import com.dianping.cat.message.spi.internal.DefaultMessageConsumerRegistry;
import com.dianping.cat.report.ReportModule;
import com.dianping.cat.report.ServerConfig;
import com.dianping.cat.report.page.failure.FailureManage;
import com.dianping.cat.report.page.ip.IpManage;
import com.dianping.cat.report.page.service.provider.FailureModelProvider;
import com.dianping.cat.report.page.service.provider.IpModelProvider;
import com.dianping.cat.report.page.service.provider.ModelProvider;
import com.dianping.cat.report.page.service.provider.TransactionModelProvider;
import com.dianping.cat.report.page.transaction.TransactionManage;
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
		      .config(E("consumerServers").value("192.168.32.68:2281,192.168.32.68:2281"))//
		      .config(E("fileServer").value("192.168.32.68")));

		all.add(C(ModelProvider.class, "failure", FailureModelProvider.class).req(MessageConsumer.class, "realtime"));

		all.add(C(ModelProvider.class, "transaction", TransactionModelProvider.class).req(MessageConsumer.class,
		      "realtime"));

		all.add(C(ModelProvider.class, "ip", IpModelProvider.class).req(MessageConsumer.class, "realtime"));

		all.add(C(FailureManage.class));

		all.add(C(TransactionManage.class));

		all.add(C(IpManage.class));

		// Please keep it last
		defineModuleRegistry(all, ReportModule.class, ReportModule.class);

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
