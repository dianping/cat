package com.dianping.cat.message.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.io.TcpSocketTest.MockMessageCodec;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class TcpSocketTestConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new TcpSocketTestConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		String tcpSocket = "tcp-socket";

		all.add(C(MessageCodec.class, tcpSocket, MockMessageCodec.class));
		all.add(C(MessageSender.class, tcpSocket, TcpSocketSender.class).is(PER_LOOKUP) //
		      .req(MessageCodec.class, tcpSocket, "m_codec") //
		      .req(MessageQueue.class, "default", "m_queue") //
		      .config(E("host").value("localhost")));
		all.add(C(MessageReceiver.class, tcpSocket, TcpSocketReceiver.class).is(PER_LOOKUP) //
				.req(MessageCodec.class, tcpSocket) //
		      .config(E("host").value("localhost")));

		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + TcpSocketTest.class.getName().replace('.', '/') + ".xml");
	}
}
