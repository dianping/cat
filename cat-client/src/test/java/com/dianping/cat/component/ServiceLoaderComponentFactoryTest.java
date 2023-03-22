package com.dianping.cat.component;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.factory.ServiceLoaderComponentFactory;

public class ServiceLoaderComponentFactoryTest {
	@Test
	public void testLookup() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new ServiceLoaderComponentFactory());

		// it's registered at /META-INF/services/, so it's okay to lookup
		Assert.assertEquals("foo", ctx.lookup(Foo.class).execute());

		// it's not registered, failed to lookup
		try {
			Assert.assertEquals("bar", ctx.lookup(Bar.class).execute());

			Assert.fail("Should catch ComponentException");
		} catch (ComponentException e) {
			// expected
		}
	}

	public interface Foo {
		String execute();
	}

	public static class DefaultFoo implements Foo {
		@Override
		public String execute() {
			return "foo";
		}
	}

	public interface Bar {
		String execute();
	}

	public static class DefaultBar implements Bar {
		@Override
		public String execute() {
			return "bar";
		}
	}
}
