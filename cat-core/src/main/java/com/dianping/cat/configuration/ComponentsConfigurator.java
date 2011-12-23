package com.dianping.cat.configuration;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.message.transport.InMemoryTransport;
import com.dianping.cat.message.transport.TcpSocketTransport;
import com.dianping.cat.message.transport.Transport;
import com.dianping.cat.message.transport.TransportManager;
import com.dianping.cat.message.transport.UdpMulticastTransport;
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
            .req(Transport.class, "in-memory"));

      return all;
   }

   public static void main(String[] args) {
      generatePlexusComponentsXmlFile(new ComponentsConfigurator());
   }
}
