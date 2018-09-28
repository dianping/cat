package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.entity.*;

public interface IMaker<T> {

    Bind buildBind(T node);

    ClientConfig buildConfig(T node);

    Domain buildDomain(T node);

    Property buildProperty(T node);

    Server buildServer(T node);
}
