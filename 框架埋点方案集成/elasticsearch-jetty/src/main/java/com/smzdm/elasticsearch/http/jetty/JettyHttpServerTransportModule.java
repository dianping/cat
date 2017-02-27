package com.smzdm.elasticsearch.http.jetty;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;

/**
 * @author zhengwen.zhu
 */
public class JettyHttpServerTransportModule extends AbstractModule {

    private final Settings settings;

    public JettyHttpServerTransportModule(Settings settings) {
        this.settings = settings;
    }

    @SuppressWarnings({"unchecked"})
    @Override protected void configure() {
    }
}
