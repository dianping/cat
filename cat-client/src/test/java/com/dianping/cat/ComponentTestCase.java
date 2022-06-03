package com.dianping.cat;

import org.junit.After;
import org.junit.Before;

public abstract class ComponentTestCase {
	protected <T> T lookup(Class<T> componentType) {
		return Cat.getBootstrap().getComponentContext().lookup(componentType);
	}

	@Before
	public void setUp() {
		Cat.getBootstrap();
	}

	@After
	public void tearDown() throws Exception {
		Cat.destroy();
	}
}
