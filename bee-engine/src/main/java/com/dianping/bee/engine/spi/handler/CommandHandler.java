package com.dianping.bee.engine.spi.handler;

import com.alibaba.cobar.server.ServerConnection;

public interface CommandHandler {
	public void handle(String sql, ServerConnection c, int offset);
}
