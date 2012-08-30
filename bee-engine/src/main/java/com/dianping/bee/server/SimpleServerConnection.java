/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-23
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

import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.Set;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.util.MySQLMessage;
import com.alibaba.cobar.protocol.mysql.OkPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.spi.session.SessionManager;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerConnection extends ServerConnection {

	private SessionManager m_sessionManager;

	/**
	 * @param channel
	 */
	public SimpleServerConnection(SocketChannel channel) {
		super(channel);
	}

	// commands --------------------------------------------------------------
	/**
	 * Override parent method in FrontendConnection
	 */
	public void initDB(byte[] data) {
		MySQLMessage mm = new MySQLMessage(data);
		mm.position(5);
		String db = mm.readString();

		if (!privileges.userExists(user, host)) {
			writeErrMessage(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + user + "'");
			return;
		}
		Set<String> schemas = privileges.getUserSchemas(user);
		if (schemas == null || schemas.size() == 0 || schemas.contains(db)) {
			this.schema = db;
			write(writeToBuffer(OkPacket.OK, allocate()));
		} else {
			String s = "Access denied for user '" + user + "' to database '" + db + "'";
			writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, s);
		}
	}

	@Override
	public void query(byte[] data) {
		m_sessionManager.getSession().setDatabase(getSchema());

		try {
			super.query(data);
		} finally {
			m_sessionManager.removeSession();
		}
	}

	@Override
	public void stmtPrepare(byte[] data) {
		m_sessionManager.getSession().setDatabase(getSchema());
		try {
			// 取得查询语句
			MySQLMessage mm = new MySQLMessage(data);
			mm.position(5);
			String sql = null;
			try {
				sql = mm.readString(charset);
			} catch (UnsupportedEncodingException e) {
				writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
				return;
			}
			if (sql == null || sql.length() == 0) {
				writeErrMessage(ErrorCode.ER_NOT_ALLOWED_COMMAND, "Empty Prepared SQL");
				return;
			}

			// 执行查询
			if (queryHandler != null) {
				((SimpleServerQueryHandler) queryHandler).prepare(sql);
			} else {
				writeErrMessage(ErrorCode.ER_YES, "Empty QueryHandler");
			}
		} finally {
			m_sessionManager.removeSession();
		}
	}

	public void setSessionManager(SessionManager sessionManager) {
		m_sessionManager = sessionManager;
	}
}
