package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.entity.*;

public interface ILinker {

    boolean onBind(ClientConfig parent, Bind bind);

    boolean onDomain(ClientConfig parent, Domain domain);

    boolean onProperty(ClientConfig parent, Property property);

    boolean onServer(ClientConfig parent, Server server);
}
