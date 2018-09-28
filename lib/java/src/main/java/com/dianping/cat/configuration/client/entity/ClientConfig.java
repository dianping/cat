package com.dianping.cat.configuration.client.entity;


import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.Constants;
import com.dianping.cat.configuration.client.IVisitor;
import com.dianping.cat.configuration.client.transform.DefaultXmlBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClientConfig extends BaseEntity<ClientConfig> {
    private String mode;
    private boolean enabled = true;
    private Boolean dumpLocked;
    private List<Server> servers = new ArrayList<Server>();
    private Map<String, Domain> domains = new LinkedHashMap<String, Domain>();
    private Bind bind;
    private List<Property> properties = new ArrayList<Property>();
    private String domain = "unknown";
    private int maxMessageSize = 5000;

    public ClientConfig() {
    }

    public ClientConfig(String domain) {
        if (domain != null) {
            this.domain = domain;
        }
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitConfig(this);
    }

    public ClientConfig addDomain(Domain domain) {
        domains.put(domain.getId(), domain);
        return this;
    }

    public ClientConfig addProperty(Property property) {
        properties.add(property);
        return this;
    }

    public ClientConfig addServer(Server server) {
        servers.add(server);
        return this;
    }

    public Domain findDomain(String id) {
        return domains.get(id);
    }

    public Server findServer(String ip) {
        for (Server server : servers) {
            if (!server.getIp().equals(ip)) {
                continue;
            }

            return server;
        }

        return null;
    }

    @Override
    public void mergeAttributes(ClientConfig other) {
        assertAttributeEquals(other, Constants.ENTITY_CONFIG, Constants.ATTR_DOMAIN, domain, other.getDomain());

        if (other.getMode() != null) {
            mode = other.getMode();
        }

        enabled = other.isEnabled();

        if (other.getDumpLocked() != null) {
            dumpLocked = other.getDumpLocked();
        }

        maxMessageSize = other.getMaxMessageSize();
    }

    public boolean removeDomain(String id) {
        if (domains.containsKey(id)) {
            domains.remove(id);
            return true;
        }

        return false;
    }

    public boolean removeServer(String ip) {
        int len = servers.size();

        for (int i = 0; i < len; i++) {
            Server server = servers.get(i);

            if (!server.getIp().equals(ip)) {
                continue;
            }

            servers.remove(i);
            return true;
        }

        return false;
    }

    public ClientConfig setBind(Bind bind) {
        this.bind = bind;
        return this;
    }

    public ClientConfig setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public ClientConfig setDumpLocked(Boolean dumpLocked) {
        this.dumpLocked = dumpLocked;
        return this;
    }

    public ClientConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public ClientConfig setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    public ClientConfig setMode(String mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public String toString() {
        return new DefaultXmlBuilder().buildXml(this);
    }
}
