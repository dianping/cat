package com.dianping.bee.server;

import java.nio.channels.SocketChannel;

import com.alibaba.cobar.CobarPrivileges;
import com.alibaba.cobar.Isolations;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.factory.FrontendConnectionFactory;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.session.BlockingSession;
import com.alibaba.cobar.server.session.NonBlockingSession;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerConnectionFactory extends FrontendConnectionFactory {
	@Override
	protected FrontendConnection getConnection(SocketChannel channel) {
		ServerConnection c = new ServerConnection(channel);

		c.setPrivileges(new CobarPrivileges());
		c.setTxIsolation(Isolations.REPEATED_READ);
		c.setQueryHandler(new SimpleServerQueryHandler(c));
		c.setSession(new BlockingSession(c));
		c.setSession2(new NonBlockingSession(c));

		return c;
	}
}
