package com.smzdm.elasticsearch.plugin.jetty;

import com.smzdm.elasticsearch.http.jetty.JettyHttpServerTransport;
import com.smzdm.elasticsearch.http.jetty.JettyHttpServerTransportModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.plugins.AbstractPlugin;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.newArrayList;

/**
 * @author zhengwen.zhu
 */
public class JettyPlugin extends AbstractPlugin {

    private final Settings settings;
    private Class<? extends HttpServerTransport> httpServerTransport;

    public JettyPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "jetty-with-cat";
    }

    @Override
    public String description() {
        return "Elasticsearch with jetty and cat";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        if (settings.getAsBoolean("http.enabled", true)) {
            httpServerTransport = settings.getAsClass("http.type", null);
            if (httpServerTransport == JettyHttpServerTransport.class) {
                modules.add(JettyHttpServerTransportModule.class);
            } else {
                httpServerTransport = null;
            }
        }
        return modules;
    }
}
