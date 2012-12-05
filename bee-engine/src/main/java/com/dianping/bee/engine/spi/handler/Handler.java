package com.dianping.bee.engine.spi.handler;

import com.alibaba.cobar.server.ServerConnection;

public interface Handler {
	public void handle(String sql, ServerConnection c, int offset);
}
