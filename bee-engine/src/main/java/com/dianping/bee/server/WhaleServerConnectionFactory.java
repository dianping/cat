/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-17
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.server;

import java.nio.channels.SocketChannel;

import com.alibaba.cobar.CobarPrivileges;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.model.SystemConfig;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.factory.FrontendConnectionFactory;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.session.BlockingSession;
import com.alibaba.cobar.server.session.NonBlockingSession;
import com.dianping.whale.cobar.server.WhaleServerQueryHandler;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class WhaleServerConnectionFactory extends FrontendConnectionFactory {
	@Override
	protected FrontendConnection getConnection(SocketChannel channel) {
		SystemConfig sys = CobarServer.getInstance().getConfig().getSystem();
		ServerConnection c = new WhaleServerConnection(channel);
		c.setPrivileges(new CobarPrivileges());
		c.setQueryHandler(new WhaleServerQueryHandler(c));
		c.setTxIsolation(sys.getTxIsolation());
		c.setSession(new BlockingSession(c));
		c.setSession2(new NonBlockingSession(c));
		return c;
	}
}
