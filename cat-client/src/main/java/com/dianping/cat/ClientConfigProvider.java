package com.dianping.cat;

import com.dianping.cat.configuration.client.entity.ClientConfig;

import java.util.ServiceLoader;

/**
 * 此为客户端配置的接口和扩展点，如具体应用要扩展，请实现此接口并在应用如下文件中指定实现类
 * META-INF\services\com.dianping.cat.ClientConfigProvider
 *
 * @author pucailin
 */
public interface ClientConfigProvider {
    public static final ClientConfigProvider INST = ServiceLoader.load(ClientConfigProvider.class).iterator().next();

    public ClientConfig getClientConfig();
}
