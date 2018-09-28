package com.dianping.cat.configuration.property.transform;

import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;

public interface ILinker {

   boolean onProperty(PropertyConfig parent, Property property);
}
