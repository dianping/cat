package com.dianping.bee.server;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendQueryHandler;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.handler.BeginHandler;
import com.alibaba.cobar.server.handler.ExplainHandler;
import com.alibaba.cobar.server.handler.KillHandler;
import com.alibaba.cobar.server.handler.SavepointHandler;
import com.alibaba.cobar.server.handler.SetHandler;
import com.alibaba.cobar.server.handler.StartHandler;
import com.dianping.bee.engine.spi.handler.internal.DescHandler;
import com.dianping.bee.engine.spi.handler.internal.SelectHandler;
import com.dianping.bee.engine.spi.handler.internal.ShowHandler;
import com.dianping.bee.engine.spi.handler.internal.UseHandler;
import com.dianping.bee.server.parse.SimpleServerParse;
import com.site.lookup.annotation.Inject;

public class SimpleServerQueryHandler implements FrontendQueryHandler {
	@Inject
	private SelectHandler m_selectHandler;

	@Inject
	private ShowHandler m_showHandler;

	@Inject
	private DescHandler m_descHandler;

	@Inject
	private UseHandler m_useHandler;

	private ServerConnection m_conn;

	@Override
	public void query(String sql) {
		ServerConnection c = m_conn;

		int rs = SimpleServerParse.parse(sql);
		switch (rs & 0xff) {
		case SimpleServerParse.EXPLAIN:
			ExplainHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.SET:
			SetHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.DESC:
			m_descHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.SHOW:
			m_showHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.SELECT:
			m_selectHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.START:
			StartHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.BEGIN:
			BeginHandler.handle(sql, c);
			break;
		case SimpleServerParse.SAVEPOINT:
			SavepointHandler.handle(sql, c);
			break;
		case SimpleServerParse.KILL:
			KillHandler.handle(sql, rs >>> 8, c);
			break;
		case SimpleServerParse.KILL_QUERY:
			c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported command");
			break;
		case SimpleServerParse.USE:
			m_useHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.COMMIT:
			c.commit();
			break;
		case SimpleServerParse.ROLLBACK:
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
