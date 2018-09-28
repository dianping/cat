package com.dianping.cat.status.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dianping.cat.status.model.Constants.ATTR_ID;
import static com.dianping.cat.status.model.Constants.ENTITY_EXTENSIONDETAIL;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtensionDetail extends BaseEntity<ExtensionDetail> {
    private String id;
    private double value;
    private Map<String, String> dynamicAttributes = new LinkedHashMap<String, String>();

    public ExtensionDetail() {
    }

    public ExtensionDetail(String id) {
        this.id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitExtensionDetail(this);
    }

    @Override
    public void mergeAttributes(ExtensionDetail other) {
        assertAttributeEquals(other, ENTITY_EXTENSIONDETAIL, ATTR_ID, id, other.getId());

        for (Map.Entry<String, String> e : other.getDynamicAttributes().entrySet()) {
            dynamicAttributes.put(e.getKey(), e.getValue());
        }

        value = other.getValue();
    }

    public void setDynamicAttribute(String name, String value) {
        dynamicAttributes.put(name, value);
    }

    public ExtensionDetail setId(String id) {
        this.id = id;
        return this;
    }

    public ExtensionDetail setValue(double value) {
        this.value = value;
        return this;
    }

}
