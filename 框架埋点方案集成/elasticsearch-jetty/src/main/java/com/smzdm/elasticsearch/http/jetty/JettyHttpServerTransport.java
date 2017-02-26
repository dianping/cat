package com.smzdm.elasticsearch.http.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.BoundTransportAddress;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.PortsRange;
import org.elasticsearch.env.Environment;
import org.elasticsearch.http.*;
import org.elasticsearch.transport.BindTransportException;

import java.io.File;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhengwen.zhu
 */
public class JettyHttpServerTransport extends AbstractLifecycleComponent<HttpServerTransport> implements HttpServerTransport {

    public static final String TRANSPORT_ATTRIBUTE = "com.sonian.elasticsearch.http.jetty.transport";

    private final NetworkService networkService;

    private final String port;

    private final String bindHost;

    private final String publishHost;

    private final String[] jettyConfig;

    private final String jettyConfigServerId;

    private final Environment environment;

    private final ClusterName clusterName;

    private final Client client;

    private volatile BoundTransportAddress boundAddress;

    private volatile Server jettyServer;

    private volatile HttpServerAdapter httpServerAdapter;

    @Inject
    public JettyHttpServerTransport(Settings settings, Environment environment, NetworkService networkService,
                                    ClusterName clusterName, Client client) {
        super(settings);
        this.environment = environment;
        this.networkService = networkService;
        this.port = componentSettings.get("port", settings.get("http.port", "9200-9300"));
        this.bindHost = componentSettings.get("bind_host", settings.get("http.bind_host", settings.get("http.host")));
        this.publishHost = componentSettings.get("publish_host", settings.get("http.publish_host", settings.get("http.host")));
        this.jettyConfig = componentSettings.getAsArray("config", new String[]{"jetty.xml"});
        this.jettyConfigServerId = componentSettings.get("server_id", "ESServer");
        this.clusterName = clusterName;
        this.client = client;
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        PortsRange portsRange = new PortsRange(port);
        final AtomicReference<Exception> lastException = new AtomicReference<Exception>();

        portsRange.iterate(new PortsRange.PortCallback() {
            @Override
            public boolean onPortNumber(int portNumber) {
                try {
                    Server server = null;
                    XmlConfiguration lastXmlConfiguration = null;
                    Object[] objs = new Object[jettyConfig.length];
                    Map<String, String> esProperties = jettySettings(bindHost, portNumber);

                    for (int i = 0; i < jettyConfig.length; i++) {
                        String configFile = jettyConfig[i];
                        URL config = environment.resolveConfig(configFile);
                        XmlConfiguration xmlConfiguration = new XmlConfiguration(config);

                        // Make ids of objects created in early configurations available
                        // in the later configurations
                        if (lastXmlConfiguration != null) {
                            xmlConfiguration.getIdMap().putAll(lastXmlConfiguration.getIdMap());
                        } else {
                            xmlConfiguration.getIdMap().put("ESServerTransport", JettyHttpServerTransport.this);
                            xmlConfiguration.getIdMap().put("ESClient", client);
                        }
                        // Inject elasticsearch properties
                        xmlConfiguration.getProperties().putAll(esProperties);

                        objs[i] = xmlConfiguration.configure();
                        lastXmlConfiguration = xmlConfiguration;
                    }
                    // Find jetty Server with id  jettyConfigServerId
                    Object serverObject = lastXmlConfiguration.getIdMap().get(jettyConfigServerId);
                    if (serverObject != null) {
                        if (serverObject instanceof Server) {
                            server = (Server) serverObject;
                        }
                    } else {
                        // For compatibility - if it's not available, find first available jetty Server
                        for (Object obj : objs) {
                            if (obj instanceof Server) {
                                server = (Server) obj;
                                break;
                            }
                        }
                    }
                    if (server == null) {
                        logger.error("Cannot find server with id [{}] in configuration files [{}]", jettyConfigServerId, jettyConfig);
                        lastException.set(new ElasticsearchException("Cannot find server with id " + jettyConfigServerId));
                        return true;
                    }

                    // Keep it for now for backward compatibility with previous versions of jetty.xml
                    server.setAttribute(TRANSPORT_ATTRIBUTE, JettyHttpServerTransport.this);

                    // Start all lifecycle objects configured by xml configurations
                    for (Object obj : objs) {
                        if (obj instanceof LifeCycle) {
                            LifeCycle lifeCycle = (LifeCycle) obj;
                            if (!lifeCycle.isRunning()) {
                                lifeCycle.start();
                            }
                        }
                    }
                    jettyServer = server;
                    lastException.set(null);
                } catch (BindException e) {
                    lastException.set(e);
                    return false;
                } catch (Exception e) {
                    logger.error("Jetty Startup Failed ", e);
                    lastException.set(e);
                    return true;
                }
                return true;
            }
        });
        if (lastException.get() != null) {
            throw new BindHttpException("Failed to bind to [" + port + "]", lastException.get());
        }
        InetSocketAddress jettyBoundAddress = findFirstInetConnector(jettyServer);
        if (jettyBoundAddress != null) {
            InetSocketAddress publishAddress;
            try {
                publishAddress = new InetSocketAddress(networkService.resolvePublishHostAddress(publishHost), jettyBoundAddress.getPort());
            } catch (Exception e) {
                throw new BindTransportException("Failed to resolve publish address", e);
            }
            this.boundAddress = new BoundTransportAddress(new InetSocketTransportAddress(jettyBoundAddress), new InetSocketTransportAddress(publishAddress));
        } else {
            throw new BindHttpException("Failed to find a jetty connector with Inet transport");
        }
    }

    private InetSocketAddress findFirstInetConnector(Server server) {
        Connector[] connectors = server.getConnectors();
        if (connectors != null) {
            for (Connector connector : connectors) {
                Object connection = connector.getConnection();
                if (connection instanceof ServerSocketChannel) {
                    SocketAddress address = ((ServerSocketChannel) connector.getConnection()).socket().getLocalSocketAddress();
                    if (address instanceof InetSocketAddress) {
                        return (InetSocketAddress) address;
                    }
                } else if (connection instanceof ServerSocket) {
                    SocketAddress address = ((ServerSocket) connector.getConnection()).getLocalSocketAddress();
                    if (address instanceof InetSocketAddress) {
                        return (InetSocketAddress) address;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        if (jettyServer != null) {
            try {
                jettyServer.stop();
            } catch (Exception ex) {
                throw new ElasticsearchException("Cannot stop jetty server", ex);
            }
            jettyServer = null;
        }
    }

    @Override
    protected void doClose() throws ElasticsearchException {
    }

    @Override
    public BoundTransportAddress boundAddress() {
        return this.boundAddress;
    }

    @Override
    public HttpInfo info() {
        return new HttpInfo(boundAddress(), 0);
    }

    @Override
    public HttpStats stats() {
        return new HttpStats(0, 0);
    }

    @Override
    public void httpServerAdapter(HttpServerAdapter httpServerAdapter) {
        this.httpServerAdapter = httpServerAdapter;
    }

    public HttpServerAdapter httpServerAdapter() {
        return httpServerAdapter;
    }

    public Settings settings() {
        return settings;
    }

    public Settings componentSettings() {
        return componentSettings;
    }

    private Map<String, String> jettySettings(String hostAddress, int port) {
        MapBuilder<String, String> jettySettings = MapBuilder.newMapBuilder();
        jettySettings.put("es.home", environment.homeFile().getAbsolutePath());
        jettySettings.put("es.config", environment.configFile().getAbsolutePath());
        jettySettings.put("es.data", getAbsolutePaths(environment.dataFiles()));
        jettySettings.put("es.cluster.data", getAbsolutePaths(environment.dataWithClusterFiles()));
        jettySettings.put("es.cluster", clusterName.value());
        if (hostAddress != null) {
            jettySettings.put("jetty.bind_host", hostAddress);
        }
        for (Map.Entry<String, String> entry : componentSettings.getAsMap().entrySet()) {
            jettySettings.put("jetty." + entry.getKey(), entry.getValue());
        }
        // Override jetty port in case we have a port-range
        jettySettings.put("jetty.port", String.valueOf(port));
        return jettySettings.immutableMap();
    }

    private String getAbsolutePaths(File[] files) {
        StringBuilder buf = new StringBuilder();
        for (File file : files) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(file.getAbsolutePath());
        }
        return buf.toString();
    }

}
