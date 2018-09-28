package com.dianping.cat.configuration.property;

import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;

public interface IVisitor {

    void visitProperty(Property property);

    void visitPropertyConfig(PropertyConfig propertyConfig);
}
