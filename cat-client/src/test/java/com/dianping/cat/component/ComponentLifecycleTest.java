package com.dianping.cat.component;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.component.ComponentContext.ComponentFactory;
import com.dianping.cat.component.ComponentContext.InstantiationStrategy;
import com.dianping.cat.component.lifecycle.Disposable;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;

public class ComponentLifecycleTest {
	@Test
	public void testDisposable() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new MyComponentFactory());

		Third forth = ctx.lookup(Third.class);

		ctx.dispose();
		Assert.assertEquals(true, forth.isDisposed());
	}

	@Test
	public void testInitializable() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new MyComponentFactory());

		Second second = ctx.lookup(Second.class);

		Assert.assertEquals(true, second.isInitialized());
	}

	@Test
	public void testLogEnabled() {
		ComponentContext ctx = new DefaultComponentContext();

		ctx.registerFactory(new MyComponentFactory());

		First first = ctx.lookup(First.class);

		Assert.assertNotNull(first.getLogger());
	}

	private static class DefaultFirst implements First, LogEnabled {
		private Logger m_logger;

		@Override
		public Logger getLogger() {
			return m_logger;
		}

		@Override
		public void enableLogging(Logger logger) {
			m_logger = logger;
		}
	}

	private static class DefaultThird implements Third, Disposable {
		private AtomicBoolean m_disposed = new AtomicBoolean();

		@Override
		public void dispose() {
			m_disposed.set(true);
		}

		@Override
		public boolean isDisposed() {
			return m_disposed.get();
		}
	}

	private static class DefaultSecond implements Second, Initializable {
		private AtomicBoolean m_initialized = new AtomicBoolean();

		@Override
		public void initialize(ComponentContext ctx) {
			m_initialized.set(true);
		}

		@Override
		public boolean isInitialized() {
			return m_initialized.get();
		}
	}

	private static interface First {
		public Logger getLogger();
	}

	private static interface Third {
		public boolean isDisposed();
	}

	private static class MyComponentFactory implements ComponentFactory {
		@Override
		public Object create(Class<?> componentType) {
			if (componentType == First.class) {
				return new DefaultFirst();
			} else if (componentType == Second.class) {
				return new DefaultSecond();
			} else if (componentType == Third.class) {
				return new DefaultThird();
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
		public boolean isInitialized();
	}
}
