package com.dianping.bee.server;

import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.alibaba.cobar.CobarPrivileges;
import com.alibaba.cobar.Isolations;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.factory.FrontendConnectionFactory;
import com.alibaba.cobar.net.handler.FrontendQueryHandler;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.session.BlockingSession;
import com.alibaba.cobar.server.session.NonBlockingSession;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.SessionManager;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerConnectionFactory extends FrontendConnectionFactory {
	private PlexusContainer m_container;

	private Set<String> m_databases;

	@Override
	protected FrontendConnection getConnection(SocketChannel channel) {
		SimpleServerConnection c = new SimpleServerConnection(channel);
		FrontendQueryHandler queryHandler = getQueryHandler(c); // TODO use
		                                                        // another one for
		                                                        // test

		c.setSessionManager(getSessionManager());
		c.setQueryHandler(queryHandler);
		c.setPrivileges(new BeeFrontendPrivileges(new CobarPrivileges(), getDatabases()));
		c.setTxIsolation(Isolations.REPEATED_READ);
		c.setSession(new BlockingSession(c));
		c.setSession2(new NonBlockingSession(c));
		return c;
	}

	private Set<String> getDatabases() {
		if (m_databases == null) {
			try {
				List<DatabaseProvider> databases = m_container.lookupList(DatabaseProvider.class);
				Set<String> databaseNames = new HashSet<String>(databases.size());
				for (DatabaseProvider provider : databases) {
					databaseNames.add(provider.getName());
				}
				m_databases = databaseNames;
			} catch (ComponentLookupException e) {
				throw new RuntimeException(
				      "Unable to get DatabaseProvider instance, please check if the environment is setup correctly!", e);
			}
		}
		return m_databases;
	}

	protected SimpleServerQueryHandler getQueryHandler(ServerConnection c) {
		try {
			SimpleServerQueryHandler queryHandler = m_container.lookup(SimpleServerQueryHandler.class);

			queryHandler.setServerConnection(c);
			return queryHandler;
		} catch (ComponentLookupException e) {
			throw new RuntimeException(
			      "Unable to get SimpleServerQueryHandler instance, please check if the environment is setup correctly!",
			      e);
		}
	}

	protected SessionManager getSessionManager() {
		try {
			SessionManager sessionManager = m_container.lookup(SessionManager.class);

			return sessionManager;
		} catch (ComponentLookupException e) {
			throw new RuntimeException(
			      "Unable to get SessionManager instance, please check if the environment is setup correctly!", e);
		}
	}

	public void setContainer(PlexusContainer container) {
		m_container = container;
	}
}
