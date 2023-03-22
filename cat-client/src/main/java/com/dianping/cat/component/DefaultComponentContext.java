package com.dianping.cat.component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dianping.cat.component.factory.ComponentFactory;
import com.dianping.cat.component.factory.ComponentFactorySupport;
import com.dianping.cat.component.factory.RoleHintedComponentFactory;
import com.dianping.cat.component.lifecycle.DefaultLogger;
import com.dianping.cat.component.lifecycle.Logger;
import com.dianping.cat.component.lifecycle.LoggerWrapper;

public class DefaultComponentContext implements ComponentContext {
	private ConcurrentMap<ComponentKey, Object> m_singletons = new ConcurrentHashMap<>();

	private List<ComponentFactory> m_factories = new ArrayList<>();

	private OverrideComponentFactory m_overrideFactory = new OverrideComponentFactory();

	private DefaultComponentLifecycle m_lifecycle;

	public DefaultComponentContext() {
		registerFactory(m_overrideFactory);
		registerFactory(new SystemComponentFactory());

		m_lifecycle = new DefaultComponentLifecycle(this);
	}

	@Override
	public void dispose() {
		for (Object component : m_singletons.values()) {
			m_lifecycle.onStop(component);
		}

		for (ComponentFactory factory : m_factories) {
			m_lifecycle.onStop(factory);
		}

		m_singletons.clear();
		m_factories.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T> T lookup(Class<T> role) {
		ComponentKey key = ComponentKey.of(role);
		Object component = m_singletons.get(key);

		if (component == null) {
			for (ComponentFactory factory : m_factories) {
				component = lookup(factory, key);

				if (component != null) {
					break;
				}
			}
		}

		if (component == null) {
			throw new ComponentException("InternalError: No component(%s) defined!", key);
		} else if (!role.isAssignableFrom(component.getClass())) {
			throw new ComponentException("InternalError: Component(%s) is not implementing %s!",
			      component.getClass().getName(), key.getRole().getName());
		}

		return (T) component;
	}

	private Object lookup(ComponentFactory factory, ComponentKey key) {
		InstantiationStrategy is = null;

		if (key.isDefault()) {
			is = factory.getInstantiationStrategy(key.getRole());
		} else if (factory instanceof RoleHintedComponentFactory) {
			is = ((RoleHintedComponentFactory) factory).getInstantiationStrategy(key.getRole(), key.getRoleHint());
		}

		if (is == null || is.isUnkown()) {
			return null;
		} else if (is.isPrototype()) { // per lookup, no instance cache
			Object component = null;

			if (key.isDefault()) {
				component = factory.create(key.getRole());
			} else if (factory instanceof RoleHintedComponentFactory) {
				component = ((RoleHintedComponentFactory) factory).create(key.getRole(), key.getRoleHint());
			}

			if (component != null) {
				m_lifecycle.onStart(component);
				return component;
			}
		} else if (is.isSingleton()) {
			Object component = null;

			if (key.isDefault()) {
				component = factory.create(key.getRole());
			} else if (factory instanceof RoleHintedComponentFactory) {
				component = ((RoleHintedComponentFactory) factory).create(key.getRole(), key.getRoleHint());
			}

			if (component != null) {
				Object comp;

				if ((comp = m_singletons.putIfAbsent(key, component)) == null) {
					comp = component;
					m_lifecycle.onStart(comp);
				}

				return comp;
			}
		}

		return null;
	}

	@Override
	public <T> List<T> lookupList(Class<T> role) {
		Map<String, T> components = lookupMap(role);

		return new ArrayList<T>(components.values());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> lookupMap(Class<T> role) {
		Map<String, Object> components = new LinkedHashMap<>();

		for (ComponentFactory factory : m_factories) {
			if (factory instanceof RoleHintedComponentFactory) {
				RoleHintedComponentFactory f = (RoleHintedComponentFactory) factory;
				List<String> roleHints = f.getRoleHints(role);

				if (roleHints != null) {
					for (String roleHint : roleHints) {
						if (!components.containsKey(roleHint)) {
							ComponentKey key = ComponentKey.of(role, roleHint);
							Object c = lookup(factory, key);

							if (c != null) {
								components.put(key.getRoleHint(), c);
							}
						}
					}
				}
			} else if (!components.containsKey(ComponentKey.DEFAULT)) {
				ComponentKey key = ComponentKey.of(role);
				Object c = lookup(factory, key);

				if (c != null) {
					components.put(key.getRoleHint(), c);
				}
			}
		}

		return (Map<String, T>) components;
	}

	public <T> void registerComponent(Class<T> role, String roleHint, T component) {
		ComponentKey key = ComponentKey.of(role, roleHint);
		Object old = m_singletons.remove(key);

		m_overrideFactory.addComponent(role, roleHint, component);

		// new Logger should reflect the existing LogEnabled component
		if (role == Logger.class && old instanceof LoggerWrapper) {
			((LoggerWrapper) old).setLogger((Logger) component);
		}
	}

	public <T> void registerComponent(Class<T> role, T component) {
		registerComponent(role, null, component);
	}

	@Override
	public void registerFactory(ComponentFactory factory) {
		if (!m_factories.contains(factory)) {
			m_factories.add(factory);
		}
	}

	private static class ComponentKey {
		public static final String DEFAULT = "default";

		private Class<?> m_role;

		private String m_roleHint;

		private ComponentKey(Class<?> role, String roleHint) {
			m_role = role;
			m_roleHint = roleHint;
		}

		public static ComponentKey of(Class<?> role) {
			return new ComponentKey(role, null);
		}

		public static ComponentKey of(Class<?> role, String roleHint) {
			return new ComponentKey(role, roleHint);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ComponentKey) {
				ComponentKey key = (ComponentKey) obj;

				if (key.m_role != m_role) {
					return false;
				}

				if (key.isDefault()) {
					return isDefault();
				} else {
					return key.m_roleHint.equals(m_roleHint);
				}
			}

			return false;
		}

		public Class<?> getRole() {
			return m_role;
		}

		public String getRoleHint() {
			if (m_roleHint == null) {
				return DEFAULT;
			} else {
				return m_roleHint;
			}
		}

		@Override
		public int hashCode() {
			int hash = 0;

			hash = 31 * hash + m_role.hashCode();
			hash = 31 * hash + (isDefault() ? 0 : m_roleHint.hashCode());

			return hash;
		}

		public boolean isDefault() {
			return m_roleHint == null || m_roleHint.equals(DEFAULT);
		}

		@Override
		public String toString() {
			if (isDefault()) {
				return m_role.getName();
			} else {
				return String.format("%s:%s", m_role.getName(), m_roleHint);
			}
		}
	}

	private static class OverrideComponentFactory extends ComponentFactorySupport {
		public <T> void addComponent(Class<T> role, String roleHint, T component) {
			singletonOf(role, roleHint).by(component);
		}

		@Override
		protected void defineComponents() {
			// nothing here
		}
	}

	private class SystemComponentFactory implements ComponentFactory {
		@Override
		public Object create(Class<?> role) {
			Object component = null;

			if (role == ComponentContext.class) {
				return DefaultComponentContext.this;
			} else if (role == ComponentLifecycle.class) {
				return m_lifecycle;
			} else if (role == Logger.class) {
				// wrap the logger so that it could be replaced
				component = new LoggerWrapper(new DefaultLogger());
			}

			return component;
		}

		@Override
		public InstantiationStrategy getInstantiationStrategy(Class<?> role) {
			return InstantiationStrategy.SINGLETON;
		}
	}
}
