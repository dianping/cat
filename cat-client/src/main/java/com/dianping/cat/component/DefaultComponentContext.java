package com.dianping.cat.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultComponentContext implements ComponentContext {
	private ConcurrentMap<Class<?>, Object> m_singletons = new ConcurrentHashMap<>();

	private List<ComponentFactory> m_factories = new ArrayList<>();

	private OverrideComponentFactory m_overrideFactory = new OverrideComponentFactory();

	private ComponentLifecycle m_lifecycle;

	public DefaultComponentContext() {
		registerFactory(m_overrideFactory);
		registerFactory(new SystemComponentFactory());

		m_lifecycle = new DefaultComponentLifecycle(this);
		m_lifecycle.initialize(this);
	}

	@Override
	public void dispose() {
		for (Object component : m_singletons.values()) {
			m_lifecycle.onStop(component);
		}
	}

	private Object findOrCreateComponent(Class<?> componentType) {
		Object component = m_singletons.get(componentType);

		if (component != null) {
			return component;
		}

		for (ComponentFactory factory : m_factories) {
			InstantiationStrategy is = factory.getInstantiationStrategy(componentType);

			if (is == null || is.isUnkown()) {
				continue;
			} else if (is.isPerLookup()) { // PER_LOOKUP no instance cache
				component = factory.create(componentType);

				if (component != null) {
					m_lifecycle.onStart(component);
					return component;
				}
			} else if (is.isSingleton()) {
				component = factory.create(componentType);

				if (component != null) {
					Object comp;

					if ((comp = m_singletons.putIfAbsent(componentType, component)) == null) {
						comp = component;
						m_lifecycle.onStart(comp);
					}

					return comp;
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T> T lookup(Class<T> componentType) {
		Object component = findOrCreateComponent(componentType);

		if (component == null) {
			throw new ComponentException("InternalError: No component(%s) defined!", componentType.getName());
		} else if (!componentType.isAssignableFrom(component.getClass())) {
			throw new ComponentException("InternalError: Component(%s) is not implementing %s!",
			      component.getClass().getName(), componentType.getName());
		}

		return (T) component;
	}

	public <T> void registerComponent(Class<T> componentType, T component) {
		m_singletons.remove(componentType);
		m_lifecycle.onStart(component);
		m_overrideFactory.addComponent(componentType, component);
	}

	@Override
	public void registerFactory(ComponentFactory factory) {
		if (!m_factories.contains(factory)) {
			m_factories.add(factory);
		}
	}

	private static class OverrideComponentFactory implements ComponentFactory {
		private Map<Class<?>, Object> m_overrides = new HashMap<>();

		public void addComponent(Class<?> componentType, Object component) {
			m_overrides.put(componentType, component);
		}

		@Override
		public Object create(Class<?> componentType) {
			Object component = m_overrides.get(componentType);

			return component;
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> componentType) {
			return InstantiationStrategy.SINGLETON;
		}
	}

	private class SystemComponentFactory implements ComponentFactory {
		@Override
		public Object create(Class<?> componentType) {
			Object component = null;

			if (componentType == ComponentContext.class) {
				return DefaultComponentContext.this;
			} else if (componentType == ComponentLifecycle.class) {
				return m_lifecycle;
			} else if (componentType == Logger.class) {
				component = new DefaultLogger();
			}

			return component;
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> componentType) {
			return InstantiationStrategy.SINGLETON;
		}
	}
}
