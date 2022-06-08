package com.dianping.cat.component.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.component.ComponentContext.InstantiationStrategy;
import com.dianping.cat.component.ComponentException;

public abstract class ComponentFactorySupport implements RoleHintedComponentFactory {
	private Map<ComponentKey, ComponentBuilder<?>> m_singletonBuilders = new HashMap<>();

	private Map<ComponentKey, ComponentBuilder<?>> m_prototypeBuilders = new HashMap<>();

	private Map<Class<?>, List<String>> m_roleHints = new HashMap<>();

	public ComponentFactorySupport() {
		defineComponents();
	}

	@Override
	public Object create(Class<?> role) {
		ComponentKey key = ComponentKey.of(role);
		ComponentBuilder<?> singleton = m_singletonBuilders.get(key);

		if (singleton != null) {
			return singleton.build();
		}

		ComponentBuilder<?> prototype = m_prototypeBuilders.get(key);

		if (prototype != null) {
			return prototype.build();
		}

		return null;
	}

	@Override
	public Object create(Class<?> role, String roleHint) {
		ComponentKey key = ComponentKey.of(role, roleHint);
		ComponentBuilder<?> singleton = m_singletonBuilders.get(key);

		if (singleton != null) {
			return singleton.build();
		}

		ComponentBuilder<?> prototype = m_prototypeBuilders.get(key);

		if (prototype != null) {
			return prototype.build();
		}

		return null;
	}

	protected abstract void defineComponents();

	@Override
	public InstantiationStrategy getInstantiationStrategy(Class<?> role) {
		ComponentKey key = ComponentKey.of(role);

		if (m_singletonBuilders.containsKey(key)) {
			return InstantiationStrategy.SINGLETON;
		} else if (m_prototypeBuilders.containsKey(key)) {
			return InstantiationStrategy.PROTOTYPE;
		} else {
			return null;
		}
	}

	@Override
	public InstantiationStrategy getInstantiationStrategy(Class<?> role, String roleHint) {
		ComponentKey key = ComponentKey.of(role, roleHint);

		if (m_singletonBuilders.containsKey(key)) {
			return InstantiationStrategy.SINGLETON;
		} else if (m_prototypeBuilders.containsKey(key)) {
			return InstantiationStrategy.PROTOTYPE;
		} else {
			return null;
		}
	}

	@Override
	public List<String> getRoleHints(Class<?> role) {
		return m_roleHints.get(role);
	}

	protected <T> ComponentBuilder<T> prototypeOf(Class<T> role) {
		ComponentBuilder<T> builder = new ComponentBuilder<T>(role);

		m_prototypeBuilders.put(ComponentKey.of(role), builder);
		return builder;
	}

	protected <T> ComponentBuilder<T> prototypeOf(Class<T> role, String roleHint) {
		ComponentBuilder<T> builder = new ComponentBuilder<T>(role).hint(roleHint);

		m_prototypeBuilders.put(ComponentKey.of(role, roleHint), builder);
		return builder;
	}

	protected <T> ComponentBuilder<T> singletonOf(Class<T> role) {
		return singletonOf(role, null);
	}

	protected <T> ComponentBuilder<T> singletonOf(Class<T> role, String roleHint) {
		if (roleHint == null) {
			roleHint = ComponentKey.DEFAULT;
		}

		ComponentBuilder<T> builder = new ComponentBuilder<T>(role).hint(roleHint);
		List<String> roleHints = m_roleHints.get(role);

		if (roleHints == null) {
			roleHints = new ArrayList<>();
			m_roleHints.put(role, roleHints);
		}

		if (!roleHints.contains(roleHint)) {
			roleHints.add(roleHint);
		}

		m_singletonBuilders.put(ComponentKey.of(role, roleHint), builder);
		return builder;
	}

	protected class ComponentBuilder<T> {
		private Class<T> m_role;

		private Class<? extends T> m_implementationClass;

		private String m_roleHint;

		private ComponentBuilder(Class<T> role) {
			m_role = role;
		}

		public Object build() {
			if (m_implementationClass != null) {
				try {
					return m_implementationClass.getDeclaredConstructor().newInstance();
				} catch (Exception e) {
					throw new ComponentException(e, "Error when creating new instance(%s:%s) from %s. %s", m_role.getName(),
					      m_roleHint, m_implementationClass, e);
				}
			}

			try {
				return m_role.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw new ComponentException(e, "Error when creating new instance(%s:%s) from %s. %s", m_role.getName(),
				      m_roleHint, m_role, e);
			}
		}

		public ComponentBuilder<T> by(Class<? extends T> implementationClass) {
			m_implementationClass = implementationClass;
			return this;
		}

		public ComponentBuilder<T> hint(String roleHint) {
			m_roleHint = roleHint;
			return this;
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
}
