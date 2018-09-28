package com.dianping.cat.configuration.property.entity;

import com.dianping.cat.configuration.property.BaseEntity;
import com.dianping.cat.configuration.property.Constants;
import com.dianping.cat.configuration.property.IVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Property extends BaseEntity<Property> {
    private String id;
    private String value;

    public Property() {
    }

    public Property(String id) {
        this.id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitProperty(this);
    }

    @Override
    public void mergeAttributes(Property other) {
        assertAttributeEquals(other, Constants.ENTITY_PROPERTY, Constants.ATTR_ID, id, other.getId());

        if (other.getValue() != null) {
            value = other.getValue();
        }
    }

    public Property setId(String id) {
        this.id = id;
        return this;
    }

    public Property setValue(String value) {
        this.value = value;
        return this;
    }

}
