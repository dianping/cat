package com.dianping.cat.component;

import com.dianping.cat.apiguardian.api.API;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface ComponentContext {
	void dispose();

	<T> T lookup(Class<T> componentType);

	<T> void registerComponent(Class<T> componentType, T component);

	void registerFactory(ComponentFactory factory);

	public interface ComponentFactory {
		Object create(Class<?> componentType);

		InstantiationStrategy getInstantiationStrategy(Class<?> componentType);
	}

	public enum InstantiationStrategy {
		SINGLETON,

		PER_LOOKUP,

		UNKNOWN;

		public boolean isPerLookup() {
			return this == PER_LOOKUP;
		}

		public boolean isSingleton() {
			return this == SINGLETON;
		}

		public boolean isUnkown() {
			return this == UNKNOWN;
		}
	}
}
