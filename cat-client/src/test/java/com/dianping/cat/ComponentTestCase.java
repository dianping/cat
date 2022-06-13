package com.dianping.cat;

import org.junit.After;
import org.junit.Before;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.message.context.MessageContextHelper;

public abstract class ComponentTestCase {
	protected ComponentContext context() {
		return Cat.getBootstrap().getComponentContext();
	}

	protected <T> T lookup(Class<T> componentType) {
		return context().lookup(componentType);
	}

	@Before
	public void setUp() throws Exception {
		Cat.getBootstrap();
		MessageContextHelper.reset();
	}

	@After
	public void tearDown() throws Exception {
		Cat.destroy();
		MessageContextHelper.reset();
	}
}
