package com.dianping.cat.message.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.io.InMemoryQueue;
import com.dianping.cat.message.io.InMemorySender;
import com.dianping.cat.message.io.MessageSender;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class MessageProducerTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new MessageProducerTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(InMemoryQueue.class, "mock", InMemoryQueue.class));
		all.add(C(MessageSender.class, "mock", InMemorySender.class) //
		      .req(InMemoryQueue.class, "mock"));
		all.add(C(MessageManager.class) //
		      .req(MessageSender.class, "mock"));

		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + MessageProducerTest.class.getName().replace('.', '/') + ".xml");
	}
}
