package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatClientModule;
import com.dianping.cat.configuration.DefaultClientConfigManager;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.io.DefaultTransportManager;
import com.dianping.cat.message.io.TcpSocketSender;
import com.dianping.cat.message.spi.internal.DefaultMessageStatistics;
import com.dianping.cat.status.StatusUpdateTask;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultClientConfigManager.class));
		all.add(C(MessageIdFactory.class));

		all.add(A(DefaultMessageManager.class));
		all.add(A(DefaultMessageProducer.class));

		all.add(A(TcpSocketSender.class));
		all.add(A(DefaultTransportManager.class));

		all.add(A(DefaultMessageStatistics.class));
		all.add(A(StatusUpdateTask.class));

		all.add(A(CatClientModule.class));

		return all;
	}
}
