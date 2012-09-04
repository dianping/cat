package com.dianping.bee.server;

import java.nio.channels.SocketChannel;

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
import com.dianping.bee.engine.spi.session.SessionManager;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerConnectionFactory extends FrontendConnectionFactory {
	private PlexusContainer m_container;

	@Override
	protected FrontendConnection getConnection(SocketChannel channel) {
		SimpleServerConnection c = new SimpleServerConnection(channel);
		FrontendQueryHandler queryHandler = getQueryHandler(c); // TODO use another one for test

		c.setSessionManager(getSessionManager());
		c.setQueryHandler(queryHandler);
		c.setPrivileges(new CobarPrivileges());
		c.setTxIsolation(Isolations.REPEATED_READ);
		c.setSession(new BlockingSession(c));
		c.setSession2(new NonBlockingSession(c));
		return c;
	}

	protected SimpleServerQueryHandler getQueryHandler(ServerConnection c) {
		try {
			SimpleServerQueryHandler queryHandler = m_container.lookup(SimpleServerQueryHandler.class);

			queryHandler.setServerConnection(c);
			return queryHandler;
		} catch (ComponentLookupException e) {
			throw new RuntimeException(
			      "Unable to get SimpleServerQueryHandler instance, please check if the environment is setup correctly!", e);
		}
	}

	protected SessionManager getSessionManager() {
		try {
			SessionManager sessionManager = m_container.lookup(SessionManager.class);

			return sessionManager;
		} catch (ComponentLookupException e) {
			throw new RuntimeException("Unable to get SessionManager instance, please check if the environment is setup correctly!", e);
		}
	}

	public void setContainer(PlexusContainer container) {
		m_container = container;
	}
}
