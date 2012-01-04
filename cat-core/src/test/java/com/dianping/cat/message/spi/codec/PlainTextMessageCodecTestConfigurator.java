package com.dianping.cat.message.spi.codec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.dianping.cat.message.io.MessageSender;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class PlainTextMessageCodecTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new PlainTextMessageCodecTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();
		String inMemory = "in-memory";

		all.add(C(MessageProducer.class, DefaultMessageProducer.class) //
		      .req(MessageSender.class, inMemory));

		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + PlainTextMessageCodecTest.class.getName().replace('.', '/') + ".xml");
	}
}
