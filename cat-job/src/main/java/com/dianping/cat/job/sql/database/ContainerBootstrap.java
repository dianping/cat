package com.dianping.cat.job.sql.database;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import org.unidal.lookup.ContainerLoader;

public enum ContainerBootstrap {
	INSTANCE;

	private PlexusContainer m_container;

	private ContainerBootstrap() {
		m_container = ContainerLoader.getDefaultContainer();
	}

	public <T> T lookup(Class<T> role) throws ComponentLookupException {
		return (T) m_container.lookup(role);
	}

	public <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException {
		return (T) m_container.lookup(role, roleHint);
	}
}
