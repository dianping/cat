package com.dianping.cat.configuration.client;

import com.dianping.cat.configuration.client.entity.*;

public interface IVisitor {

    void visitBind(Bind bind);

    void visitConfig(ClientConfig config);

    void visitDomain(Domain domain);

    void visitProperty(Property property);

    void visitServer(Server server);
}
