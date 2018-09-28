package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.IVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Bind extends BaseEntity<Bind> {
    private String ip;
    private int port;

    public Bind() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitBind(this);
    }

    @Override
    public void mergeAttributes(Bind other) {
        if (other.getIp() != null) {
            ip = other.getIp();
        }

        port = other.getPort();
    }

    public Bind setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Bind setPort(int port) {
        this.port = port;
        return this;
    }

}
