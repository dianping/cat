package com.dianping.cat.configuration.property.transform;

import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;

public interface IMaker<T> {

    Property buildProperty(T node);

    PropertyConfig buildPropertyConfig(T node);
}
