package com.dianping.cat.status.model.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import static com.dianping.cat.status.model.Constants.ATTR_KEY;
import static com.dianping.cat.status.model.Constants.ENTITY_CUSTOMINFO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomInfo extends BaseEntity<CustomInfo> {
    private String key;
    private String value;

    public CustomInfo() {
    }

    public CustomInfo(String key) {
        this.key = key;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitCustomInfo(this);
    }

    @Override
    public void mergeAttributes(CustomInfo other) {
        assertAttributeEquals(other, ENTITY_CUSTOMINFO, ATTR_KEY, key, other.getKey());

        if (other.getValue() != null) {
            value = other.getValue();
        }
    }

    public CustomInfo setKey(String key) {
        this.key = key;
        return this;
    }

    public CustomInfo setValue(String value) {
        this.value = value;
        return this;
    }

}
