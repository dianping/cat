package com.dianping.cat;

import org.junit.After;
import org.junit.Before;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.message.context.MetricContextHelper;
import com.dianping.cat.message.context.TraceContextHelper;

public abstract class ComponentTestCase {
	protected ComponentContext context() {
		return Cat.getBootstrap().getComponentContext();
	}

	protected <T> T lookup(Class<T> componentType) {
		return context().lookup(componentType);
	}

	@Before
	public void setUp() throws Exception {
		Cat.destroy();
		Cat.getBootstrap().testMode();
		TraceContextHelper.reset();
		MetricContextHelper.reset();
	}

	@After
	public void tearDown() throws Exception {
		Cat.destroy();
		TraceContextHelper.reset();
		MetricContextHelper.reset();
	}
}
