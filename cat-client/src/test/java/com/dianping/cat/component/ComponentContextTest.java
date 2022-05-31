package com.dianping.cat.component;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.ComponentContext.ComponentFactory;
import com.dianping.cat.component.ComponentContext.InstantiationStrategy;

public class ComponentContextTest {
	@Test
	public void testCustomFactory() {
		ComponentContext ctx = new DefaultComponentContext();

		try {
			ctx.lookup(First.class);
			Assert.fail("Component should not be found!");
		} catch (Exception e) {
			// expected
		}

		ctx.registerFactory(new MyComponentFactory());

		Assert.assertNotNull(ctx.lookup(First.class));

		Assert.assertNotNull(ctx.lookup(Second.class));
		Assert.assertNotSame(ctx.lookup(Second.class), ctx.lookup(Second.class));
	}

	@Test
	public void testOverrideFactory() {
		ComponentContext ctx = new DefaultComponentContext();

		try {
			ctx.lookup(Object.class);
			Assert.fail("Component should not be found!");
		} catch (Exception e) {
			// expected
		}

		ctx.registerComponent(Integer.class, Integer.valueOf(123));
		ctx.registerComponent(Object.class, Integer.valueOf(124));

		Assert.assertSame(123, ctx.lookup(Integer.class).intValue());
		Assert.assertSame(124, ctx.lookup(Object.class));

	}

	@Test
	public void testSystemFactory() {
		ComponentContext ctx = new DefaultComponentContext();

		Assert.assertNotNull(ctx.lookup(ComponentContext.class));
		Assert.assertNotNull(ctx.lookup(ComponentLifecycle.class));
		Assert.assertNotNull(ctx.lookup(Logger.class));

		// singleton
		Assert.assertSame(ctx.lookup(ComponentContext.class), ctx.lookup(ComponentContext.class));
		Assert.assertSame(ctx.lookup(ComponentLifecycle.class), ctx.lookup(ComponentLifecycle.class));
		Assert.assertSame(ctx.lookup(Logger.class), ctx.lookup(Logger.class));
	}

	private static class DefaultFirst implements First {
	}

	private static class DefaultSecond implements Second {
	}

	private static interface First {
	}

	private static class MyComponentFactory implements ComponentFactory {
		@Override
		public Object create(Class<?> componentType) {
			if (componentType == First.class) {
				return new DefaultFirst();
			} else if (componentType == Second.class) {
				return new DefaultSecond();
			}

			return null;
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> componentType) {
			if (componentType == Second.class) {
				return InstantiationStrategy.PER_LOOKUP;
			}

			return InstantiationStrategy.SINGLETON;
		}
	}

	private static interface Second {
	}
}
