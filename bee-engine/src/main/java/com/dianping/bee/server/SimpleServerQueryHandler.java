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

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendQueryHandler;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.handler.BeginHandler;
import com.alibaba.cobar.server.handler.ExplainHandler;
import com.alibaba.cobar.server.handler.KillHandler;
import com.alibaba.cobar.server.handler.SavepointHandler;
import com.alibaba.cobar.server.handler.SetHandler;
import com.alibaba.cobar.server.handler.ShowHandler;
import com.alibaba.cobar.server.handler.StartHandler;
import com.alibaba.cobar.server.handler.UseHandler;
import com.alibaba.cobar.server.parser.ServerParse;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerQueryHandler implements FrontendQueryHandler {
	@Inject
	private SelectHandler m_selectHandler;

	private static final Logger LOGGER = Logger.getLogger(SimpleServerQueryHandler.class);

	private ServerConnection m_conn;

	@Override
	public void query(String sql) {
		ServerConnection c = this.m_conn;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(new StringBuilder().append(c).append(sql).toString());
		}

		int rs = ServerParse.parse(sql);
		switch (rs & 0xff) {
		case ServerParse.EXPLAIN:
			ExplainHandler.handle(sql, c, rs >>> 8);
			break;
		case ServerParse.SET:
			SetHandler.handle(sql, c, rs >>> 8);
			break;
		case ServerParse.SHOW:
			ShowHandler.handle(sql, c, rs >>> 8);
			break;
		case ServerParse.SELECT:
			m_selectHandler.handle(sql, c, rs >>> 8);
			break;
		case ServerParse.START:
			StartHandler.handle(sql, c, rs >>> 8);
			break;
		case ServerParse.BEGIN:
			BeginHandler.handle(sql, c);
			break;
		case ServerParse.SAVEPOINT:
			SavepointHandler.handle(sql, c);
			break;
		case ServerParse.KILL:
			KillHandler.handle(sql, rs >>> 8, c);
			break;
		case ServerParse.KILL_QUERY:
			c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
			break;
		case ServerParse.USE:
			UseHandler.handle(sql, c, rs >>> 8);
			break;
		case ServerParse.COMMIT:
			c.commit();
			break;
		case ServerParse.ROLLBACK:
			c.rollback();
			break;
		default:
			c.execute(sql, rs);
		}
	}

	public void setServerConnection(ServerConnection c) {
		m_conn = c;
	}
}
