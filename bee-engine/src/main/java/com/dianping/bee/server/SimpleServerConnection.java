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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.util.MySQLMessage;
import com.alibaba.cobar.protocol.mysql.OkPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.SessionManager;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerConnection extends ServerConnection {

	private static final Logger LOGGER = Logger.getLogger(SimpleServerConnection.class);

	private SessionManager m_sessionManager;

	/**
	 * @param channel
	 */
	public SimpleServerConnection(SocketChannel channel) {
		super(channel);
	}

	@Override
	public boolean close() {
		((SimpleServerQueryHandler) queryHandler).close();

		return super.close();
	}

	// commands --------------------------------------------------------------
	/**
	 * Override parent method in FrontendConnection
	 */
	public void initDB(byte[] data) {
		LOGGER.info("initDB : " + data);
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
		LOGGER.info("StmtPrepare : " + data);
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
				((SimpleServerQueryHandler) queryHandler).stmtPrepare(sql);
			} else {
				writeErrMessage(ErrorCode.ER_YES, "Empty QueryHandler");
			}
		} finally {
			m_sessionManager.removeSession();
		}
	}

	@SuppressWarnings("unused")
   @Override
	public void stmtExecute(byte[] data) {
		LOGGER.info("StmtExecute : " + data);
		m_sessionManager.getSession().setDatabase(getSchema());
		try {
			// 取得查询语句
			MySQLMessage mm = new MySQLMessage(data);
			Long stmtId;
			mm.position(5);
			stmtId = mm.readUB4();
			byte flag = mm.read();
			int iterationCount = mm.readInt();

			// 执行查询
			if (queryHandler != null) {
				PreparedStatement stmt = ((SimpleServerQueryHandler) queryHandler).getStatement(stmtId);
				if (stmt == null) {
					writeErrMessage(ErrorCode.ER_YES, "Invalid Statement Identifier");
					return;
				}

				int parameterSize = stmt.getParameterSize();
				List<Object> parameters = new ArrayList<Object>(parameterSize);
				int nullBitMapSize = (parameterSize + 7) / 8;
				for (int i = 0; i < nullBitMapSize; i++) {
					// TODO
					byte null_bits_map = mm.read();
				}

				byte new_params_bound_flag = mm.read();
				if (new_params_bound_flag == (byte) 1) {
					for (int i = 0; i < parameterSize; i++) {
						byte[] typeArray = mm.readBytes(2);
						// TODO determine type
					}

					for (int i = 0; i < parameterSize; i++) {
						byte length = mm.read();
						byte[] value = mm.readBytes(length);
						parameters.add(new String(value));
					}
				}
				((SimpleServerQueryHandler) queryHandler).stmtExecute(stmtId, parameters);
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
