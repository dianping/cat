package com.dianping.cat.notify.server;

public interface ContainerHolder {

	<T> T lookup(Class<T> classType, String beanName);

	<T> boolean hasComponent(Class<T> classType, String beanName);

}
