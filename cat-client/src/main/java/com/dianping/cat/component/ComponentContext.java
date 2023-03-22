package com.dianping.cat.component;

import java.util.List;
import java.util.Map;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.component.factory.ComponentFactory;

@API(status = API.Status.INTERNAL, since = "3.1.0")
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
	@API(status = API.Status.INTERNAL, since = "3.1.0")
	<T> T lookup(Class<T> role);

	/**
	 * Lookup a list of the component instances for the given <code>role</code>.
	 * 
	 * @param <T>
	 * @param role
	 *           component role to look up
	 * @return a list of the component instances
	 */
	@API(status = API.Status.INTERNAL, since = "3.1.0")
	<T> List<T> lookupList(Class<T> role);

	/**
	 * Lookup a map of the component instances for the given <code>role</code>.
	 * 
	 * @param <T>
	 * @param role
	 *           component role to look up
	 * @return a map of the component instances with roleHint as key
	 */
	@API(status = API.Status.INTERNAL, since = "3.1.0")
	<T> Map<String, T> lookupMap(Class<T> role);

	@API(status = API.Status.INTERNAL, since = "3.2.0")
	<T> void registerComponent(Class<T> role, String roleHint, T component);

	@API(status = API.Status.INTERNAL, since = "3.1.0")
	<T> void registerComponent(Class<T> role, T component);

	@API(status = API.Status.INTERNAL, since = "3.1.0")
	void registerFactory(ComponentFactory factory);

	@API(status = API.Status.INTERNAL, since = "3.1.0")
	public enum InstantiationStrategy {
		SINGLETON,

		PROTOTYPE,

		UNKNOWN;

		public boolean isPrototype() {
			return this == PROTOTYPE;
		}

		public boolean isSingleton() {
			return this == SINGLETON;
		}

		public boolean isUnkown() {
			return this == UNKNOWN;
		}
	}
}
