package com.dianping.cat.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.ComponentContext.InstantiationStrategy;
import com.dianping.cat.component.factory.ComponentFactory;
import com.dianping.cat.component.factory.RoleHintedComponentFactory;
import com.dianping.cat.component.lifecycle.Logger;

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
	public void testLookupList() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new MyComponentFactory());
		ctx.registerFactory(new OtherComponentFactory());
		ctx.registerFactory(new AnotherComponentFactory());

		ctx.registerComponent(First.class, new DefaultFirst());

		Assert.assertEquals(3, ctx.lookupList(First.class).size());
		Assert.assertEquals(3, ctx.lookupList(Second.class).size());
	}

	@Test
	public void testLookupMap() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new MyComponentFactory());
		ctx.registerFactory(new OtherComponentFactory());
		ctx.registerFactory(new AnotherComponentFactory());

		ctx.registerComponent(First.class, new DefaultFirst());

		Assert.assertEquals(3, ctx.lookupList(First.class).size());
		Assert.assertEquals(3, ctx.lookupList(Second.class).size());
	}

	@Test
	public void testLookupMix() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new MyComponentFactory());
		ctx.registerFactory(new OtherComponentFactory());
		ctx.registerFactory(new AnotherComponentFactory());

		// First
		First first = ctx.lookup(First.class); // singleton
		List<First> firstList = ctx.lookupList(First.class);
		Map<String, First> firstMap = ctx.lookupMap(First.class);

		Assert.assertEquals(first, ctx.lookup(First.class));
		Assert.assertEquals(true, firstList.contains(first));
		Assert.assertEquals(first, firstMap.get("default"));

		// Second
		Second second = ctx.lookup(Second.class); // per-lookup
		List<Second> secondList = ctx.lookupList(Second.class);
		Map<String, Second> secondMap = ctx.lookupMap(Second.class);

		Assert.assertNotEquals(second, ctx.lookup(Second.class));
		Assert.assertNotEquals(second, secondMap.get("default"));
		Assert.assertTrue(secondList.contains(secondMap.get("other")));
		Assert.assertTrue(secondList.contains(secondMap.get("another")));
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

	private static class AnotherComponentFactory implements RoleHintedComponentFactory {
		@Override
		public Object create(Class<?> role) {
			return create(role, null);
		}

		@Override
		public Object create(Class<?> role, String roleHint) {
			if ("another".equals(roleHint) || "default".equals(roleHint)) {
				if (role == First.class) {
					return new DefaultFirst();
				} else if (role == Second.class) {
					return new DefaultSecond();
				}
			}

			return null;
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> role) {
			return getInstantiationStrategy(role, null);
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> role, String roleHint) {
			if (role == First.class) {
				return InstantiationStrategy.PROTOTYPE;
			}

			return InstantiationStrategy.SINGLETON;
		}

		@Override
		public List<String> getRoleHints(Class<?> role) {
			if (role == First.class) {
				return Arrays.asList("another", "default");
			} else if (role == Second.class) {
				return Arrays.asList("another", "default");
			}

			return Collections.emptyList();
		}
	}

	private static class DefaultFirst implements First {
	}

	private static class DefaultSecond implements Second {
	}

	private static interface First {
	}

	private static class MyComponentFactory implements ComponentFactory {
		@Override
		public Object create(Class<?> role) {
			if (role == First.class) {
				return new DefaultFirst();
			} else if (role == Second.class) {
				return new DefaultSecond();
			}

			return null;
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> role) {
			if (role == Second.class) {
				return InstantiationStrategy.PROTOTYPE;
			}

			return InstantiationStrategy.SINGLETON;
		}
	}

	private static class OtherComponentFactory implements RoleHintedComponentFactory {
		@Override
		public Object create(Class<?> role) {
			return create(role, null);
		}

		@Override
		public Object create(Class<?> role, String roleHint) {
			if ("other".equals(roleHint)) {
				if (role == First.class) {
					return new DefaultFirst();
				} else if (role == Second.class) {
					return new DefaultSecond();
				}
			}

			return null;
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> role) {
			return getInstantiationStrategy(role, null);
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> role, String roleHint) {
			if (role == First.class) {
				return InstantiationStrategy.PROTOTYPE;
			}

			return InstantiationStrategy.SINGLETON;
		}

		@Override
		public List<String> getRoleHints(Class<?> role) {
			if (role == First.class) {
				return Arrays.asList("other");
			} else if (role == Second.class) {
				return Arrays.asList("other");
			}

			return Collections.emptyList();
		}
	}

	private static interface Second {
	}
}
