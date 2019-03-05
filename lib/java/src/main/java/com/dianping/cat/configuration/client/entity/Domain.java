package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.Constants;
import com.dianping.cat.configuration.client.IVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Domain extends BaseEntity<Domain> {
    private String id;
    private String ip;
    private boolean enabled = true;
    private int maxMessageSize = 1000;

    public Domain() {
    }

    public Domain(String id) {
        this.id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitDomain(this);
    }

    @Override
    public void mergeAttributes(Domain other) {
        assertAttributeEquals(other, Constants.ENTITY_DOMAIN, Constants.ATTR_ID, id, other.getId());

        if (other.getIp() != null) {
            ip = other.getIp();
        }

        enabled = other.isEnabled();

        maxMessageSize = other.getMaxMessageSize();
    }

    public Domain setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Domain setId(String id) {
        this.id = id;
        return this;
    }

    public Domain setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Domain setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

}
