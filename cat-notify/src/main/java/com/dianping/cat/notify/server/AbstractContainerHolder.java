package com.dianping.cat.notify.server;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public abstract class AbstractContainerHolder implements ContainerHolder, BeanFactoryAware {

	private BeanFactory m_beanFactory;

	@SuppressWarnings("unchecked")
	public <T> T lookup(Class<T> classType, String beanName) {
		return (T) m_beanFactory.getBean(beanName, classType);
	}

	public <T> boolean hasComponent(Class<T> classType, String beanName) {
		T component = lookup(classType, beanName);
		if (component == null) {
			return false;
		}
		return true;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		m_beanFactory = beanFactory;
	}

}
