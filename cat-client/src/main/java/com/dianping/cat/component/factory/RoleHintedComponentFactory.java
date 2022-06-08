package com.dianping.cat.component.factory;

import java.util.List;

import com.dianping.cat.component.ComponentContext.InstantiationStrategy;

public interface RoleHintedComponentFactory extends ComponentFactory {
	Object create(Class<?> role, String roleHint);

	InstantiationStrategy getInstantiationStrategy(Class<?> role, String roleHint);

	List<String> getRoleHints(Class<?> role);
}