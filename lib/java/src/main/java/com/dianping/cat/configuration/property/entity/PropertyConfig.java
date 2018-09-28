package com.dianping.cat.configuration.property.entity;

import com.dianping.cat.configuration.property.BaseEntity;
import com.dianping.cat.configuration.property.IVisitor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class PropertyConfig extends BaseEntity<PropertyConfig> {
    private Map<String, Property> properties = new LinkedHashMap<String, Property>();

    public PropertyConfig() {
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitPropertyConfig(this);
    }

    public PropertyConfig addProperty(Property property) {
        properties.put(property.getId(), property);
        return this;
    }

    public Property findProperty(String id) {
        return properties.get(id);
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    @Override
    public void mergeAttributes(PropertyConfig other) {
    }

    public boolean removeProperty(String id) {
        if (properties.containsKey(id)) {
            properties.remove(id);
            return true;
        }

        return false;
    }

}
