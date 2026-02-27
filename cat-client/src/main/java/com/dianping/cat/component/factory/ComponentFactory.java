package com.dianping.cat.component.factory;

import com.dianping.cat.component.ComponentContext.InstantiationStrategy;

public interface ComponentFactory {
	Object create(Class<?> role);

	InstantiationStrategy getInstantiationStrategy(Class<?> role);
}