package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.Constants;
import com.dianping.cat.configuration.client.IVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Server extends BaseEntity<Server> {
    private String ip;
    private int port = 2280;
    private int httpPort = 8080;
    private boolean enabled = true;

    public Server() {
    }

    public Server(String ip) {
        this.ip = ip;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitServer(this);
    }

    @Override
    public void mergeAttributes(Server other) {
        assertAttributeEquals(other, Constants.ENTITY_SERVER, Constants.ATTR_IP, ip, other.getIp());

        port = other.getPort();

        httpPort = other.getHttpPort();

        enabled = other.isEnabled();
    }

    public Server setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Server setHttpPort(int httpPort) {
        this.httpPort = httpPort;
        return this;
    }

    public Server setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Server setPort(int port) {
        this.port = port;
        return this;
    }

}
