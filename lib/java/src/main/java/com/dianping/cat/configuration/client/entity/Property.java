package com.dianping.cat.configuration.client.entity;

import com.dianping.cat.configuration.client.BaseEntity;
import com.dianping.cat.configuration.client.IVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Property extends BaseEntity<Property> {
    private String name;
    private String text;

    public Property() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitProperty(this);
    }

    @Override
    public void mergeAttributes(Property other) {
        if (other.getName() != null) {
            name = other.getName();
        }
    }

    public Property setName(String name) {
        this.name = name;
        return this;
    }

    public Property setText(String text) {
        this.text = text;
        return this;
    }

}
