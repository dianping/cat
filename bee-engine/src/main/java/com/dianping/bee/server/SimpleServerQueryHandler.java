package com.dianping.bee.server;

import java.util.List;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendQueryHandler;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.handler.BeginHandler;
import com.alibaba.cobar.server.handler.ExplainHandler;
import com.alibaba.cobar.server.handler.KillHandler;
import com.alibaba.cobar.server.handler.SavepointHandler;
import com.alibaba.cobar.server.handler.SetHandler;
import com.alibaba.cobar.server.handler.StartHandler;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.handler.DescHandler;
import com.dianping.bee.engine.spi.handler.PrepareHandler;
import com.dianping.bee.engine.spi.handler.SelectHandler;
import com.dianping.bee.engine.spi.handler.ShowHandler;
import com.dianping.bee.engine.spi.handler.UseHandler;
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

	@Inject
	private PrepareHandler m_prepareHandler;

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

	/**
	 * @param sql
	 */
	public void stmtClose(Long stmtId) {
		ServerConnection c = m_conn;
		m_prepareHandler.close(stmtId, c);
	}

	/**
	 * @param sql
	 * @param parameters
	 */
	public void stmtExecute(Long stmtId, List<Object> parameters) {
		ServerConnection c = m_conn;
		m_prepareHandler.execute(stmtId, parameters, c);
	}

	public void stmtPrepare(String sql) {
		ServerConnection c = m_conn;
		m_prepareHandler.prepare(sql, c);
	}

	public PreparedStatement getStatement(Long stmtId) {
		return m_prepareHandler.getStatement(stmtId);
	}
}
