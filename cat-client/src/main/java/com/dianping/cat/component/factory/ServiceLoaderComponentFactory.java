package com.dianping.cat.component.factory;

import java.util.ServiceLoader;

import com.dianping.cat.apiguardian.api.API;
import com.dianping.cat.apiguardian.api.API.Status;
import com.dianping.cat.component.ComponentContext.InstantiationStrategy;

@API(status = Status.INTERNAL, since = "3.1.0")
public class ServiceLoaderComponentFactory implements ComponentFactory {
	@Override
	public Object create(Class<?> role) {
		try {
			ServiceLoader<?> instances = ServiceLoader.load(role);

			for (Object instance : instances) {
				return instance;
			}
		} catch (Exception e) {
			System.err.println(e);
		}

		return null;
	}

	@Override
	public InstantiationStrategy getInstantiationStrategy(Class<?> role) {
		return InstantiationStrategy.SINGLETON;
	}
}
