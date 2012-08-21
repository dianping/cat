/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-15
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

import java.io.IOException;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarConfig;
import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.config.model.SystemConfig;
import com.alibaba.cobar.net.NIOAcceptor;
import com.dianping.whale.server.config.WhaleServerConfig;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class WhaleServer {

	public static final String NAME = "Whale";

	public static final String VERSION = "0.0.1";

	private static final CobarServer COBAR_INSTANCE = CobarServer.getInstance();

	private static final WhaleServer WHALE_INSTANCE = new WhaleServer();

	private static final Logger LOGGER = Logger.getLogger(WhaleServer.class);

	public static final WhaleServer getInstance() {
		return WHALE_INSTANCE;
	}

	private final WhaleServerConfig whaleConfig;

	private WhaleServer() {
		this.whaleConfig = new WhaleServerConfig();
	}

	public void beforeStart(String dateFormat) {
		COBAR_INSTANCE.beforeStart(dateFormat);
	}

	/**
	 * @return
	 */
	public CobarConfig getConfig() {
		return COBAR_INSTANCE.getConfig();
	}

	public void startup() throws IOException {
		COBAR_INSTANCE.startup();

		whaleConfig.load();
		SystemConfig system = COBAR_INSTANCE.getConfig().getSystem();
		// startup server
		WhaleServerConnectionFactory sf = new WhaleServerConnectionFactory();
		sf.setCharset(system.getCharset());
		sf.setIdleTimeout(system.getIdleTimeout());

		NIOAcceptor server = new NIOAcceptor(NAME + "Server", whaleConfig.getWhalePort(), sf);
		server.setProcessors(COBAR_INSTANCE.getProcessors());
		server.start();

	}

}
