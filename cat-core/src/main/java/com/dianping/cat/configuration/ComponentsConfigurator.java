package com.dianping.cat.configuration;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.transport.InMemoryTransport;
import com.dianping.cat.transport.TcpSocketTransport;
import com.dianping.cat.transport.Transport;
import com.dianping.cat.transport.TransportManager;
import com.dianping.cat.transport.UdpMulticastTransport;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(Transport.class, "in-memory", InMemoryTransport.class));
		all.add(C(Transport.class, "tcp-socket", TcpSocketTransport.class));
		all.add(C(Transport.class, "udp-multicast", UdpMulticastTransport.class));

		all.add(C(TransportManager.class) //
		      .req(Transport.class, "im-memory"));

		return all;
	}

	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}
}
