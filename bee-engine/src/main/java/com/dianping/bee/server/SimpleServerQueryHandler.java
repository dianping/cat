package com.dianping.bee.server;

import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendQueryHandler;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.handler.BeginHandler;
import com.alibaba.cobar.server.handler.ExplainHandler;
import com.alibaba.cobar.server.handler.KillHandler;
import com.alibaba.cobar.server.handler.SavepointHandler;
import com.alibaba.cobar.server.handler.StartHandler;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.handler.DescHandler;
import com.dianping.bee.engine.spi.handler.PrepareHandler;
import com.dianping.bee.engine.spi.handler.SelectHandler;
import com.dianping.bee.engine.spi.handler.SetHandler;
import com.dianping.bee.engine.spi.handler.ShowHandler;
import com.dianping.bee.engine.spi.handler.UseHandler;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class SimpleServerQueryHandler extends ContainerHolder implements FrontendQueryHandler {

	private static final Logger LOGGER = Logger.getLogger(SimpleServerQueryHandler.class);

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

	@Inject
	private SetHandler m_setHandler;

	private ServerConnection m_conn;

	public void close() {
		release(this);
	}

	public PreparedStatement getStatement(Long stmtId) {
		return m_prepareHandler.getStatement(stmtId);
	}

	@Override
	public void query(String sql) {
		ServerConnection c = m_conn;
		LOGGER.info("query : " + sql);
		int rs = SimpleServerParse.parse(sql);
		switch (rs & 0xff) {
		case SimpleServerParse.EXPLAIN:
			ExplainHandler.handle(sql, c, rs >>> 8);
			break;
		case SimpleServerParse.SET:
			m_setHandler.handle(sql, c, rs >>> 8);
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

	public void stmtExecute(Long stmtId, List<Object> parameters) {
		ServerConnection c = m_conn;
		m_prepareHandler.execute(stmtId, parameters, c);
	}

	public void stmtPrepare(String sql) {
		ServerConnection c = m_conn;
		m_prepareHandler.prepare(sql, c);
	}
}
