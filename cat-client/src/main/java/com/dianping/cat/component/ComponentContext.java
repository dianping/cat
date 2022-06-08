package com.dianping.cat.component;

import java.util.List;
import java.util.Map;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.component.factory.ComponentFactory;

@API(status = API.Status.INTERNAL, since = "3.1")
public interface ComponentContext {
	void dispose();

	/**
	 * Look up the first component instance for the given <code>role</code>.
	 * 
	 * @param <T>
	 * @param role
	 *           component role to look up
	 * @return the first component instance
	 */
	<T> T lookup(Class<T> role);

	/**
	 * Lookup a list of the component instances for the given <code>role</code>.
	 * 
	 * @param <T>
	 * @param role
	 *           component role to look up
	 * @return a list of the component instances
	 */
	<T> List<T> lookupList(Class<T> role);

	/**
	 * Lookup a map of the component instances for the given <code>role</code>.
	 * 
	 * @param <T>
	 * @param role
	 *           component role to look up
	 * @return a map of the component instances with roleHint as key
	 */
	<T> Map<String, T> lookupMap(Class<T> role);

	<T> void registerComponent(Class<T> role, T component);

	void registerFactory(ComponentFactory factory);

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
