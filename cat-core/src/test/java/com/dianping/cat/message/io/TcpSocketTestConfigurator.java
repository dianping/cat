package com.dianping.cat.message.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.io.TcpSocketTest.MockMessageCodec;
import com.dianping.cat.message.spi.MessageCodec;
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
		all.add(C(MessageSender.class, tcpSocket, TcpSocketSender.class) //
		      .req(MessageCodec.class, tcpSocket) //
		      .config(E("host").value("localhost")));
		all.add(C(MessageReceiver.class, tcpSocket, TcpSocketReceiver.class) //
				.req(MessageCodec.class, tcpSocket) //
		      .config(E("host").value("localhost")));

		return all;
	}

	@Override
	protected File getConfigurationFile() {
		return new File("src/test/resources/" + TcpSocketTest.class.getName().replace('.', '/') + ".xml");
	}
}
