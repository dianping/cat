package com.dianping.cat;

public abstract class ComponentTestCase {
	protected <T> T lookup(Class<T> componentType) {
		return Cat.getBootstrap().getComponentContext().lookup(componentType);
	}
}
