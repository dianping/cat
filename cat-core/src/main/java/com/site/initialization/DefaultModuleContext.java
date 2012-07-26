package com.site.initialization;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public class DefaultModuleContext implements ModuleContext {
	private PlexusContainer m_container;

	private Map<String, Object> m_attributes;

	private Logger m_logger;

	public DefaultModuleContext(PlexusContainer container) {
		m_container = container;
		m_attributes = new HashMap<String, Object>();

		try {
			LoggerManager loggerManager = container.lookup(LoggerManager.class);

			m_logger = loggerManager.getLoggerForComponent(PlexusContainer.class.getName());
		} catch (Exception e) {
			throw new RuntimeException("Unable to get instance of Logger, "
			      + "please make sure the environment was setup correctly!", e);
		}
	}

	@Override
	public void error(String message) {
		m_logger.error(message);
	}

	@Override
	public void error(String message, Throwable e) {
		m_logger.error(message, e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) {
		return (T) m_attributes.get(name);
	}

	@Override
	public void info(String message) {
		m_logger.info(message);
	}

	@Override
	public <T> T lookup(Class<T> role) {
		return lookup(role, null);
	}

	@Override
	public <T> T lookup(Class<T> role, String roleHint) {
		try {
			return m_container.lookup(role, roleHint);
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get component: " + role + ".", e);
		}
	}

	@Override
	public void release(Object component) {
		try {
			m_container.release(component);
		} catch (ComponentLifecycleException e) {
			throw new RuntimeException("Unable to release component: " + component + ".", e);
		}
	}

	@Override
	public void setAttribute(String name, Object value) {
		m_attributes.put(name, value);
	}

	@Override
	public void warn(String message) {
		m_logger.warn(message);
	}

	@Override
	public Module[] getModules(String... names) {
		Module[] modules = new Module[names.length];
		int index = 0;

		for (String name : names) {
			modules[index++] = lookup(Module.class, name);
		}

		return modules;
	}

	public PlexusContainer getContainer() {
		return m_container;
	}
}
