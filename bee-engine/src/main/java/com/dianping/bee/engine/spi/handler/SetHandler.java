/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-13
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
package com.dianping.bee.engine.spi.handler;

import static com.alibaba.cobar.server.parser.ServerParseSet.AUTOCOMMIT_OFF;
import static com.alibaba.cobar.server.parser.ServerParseSet.AUTOCOMMIT_ON;
import static com.alibaba.cobar.server.parser.ServerParseSet.CHARACTER_SET_CLIENT;
import static com.alibaba.cobar.server.parser.ServerParseSet.CHARACTER_SET_CONNECTION;
import static com.alibaba.cobar.server.parser.ServerParseSet.CHARACTER_SET_RESULTS;
import static com.alibaba.cobar.server.parser.ServerParseSet.NAMES;
import static com.alibaba.cobar.server.parser.ServerParseSet.TX_READ_COMMITTED;
import static com.alibaba.cobar.server.parser.ServerParseSet.TX_READ_UNCOMMITTED;
import static com.alibaba.cobar.server.parser.ServerParseSet.TX_REPEATED_READ;
import static com.alibaba.cobar.server.parser.ServerParseSet.TX_SERIALIZABLE;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Isolations;
import com.alibaba.cobar.protocol.mysql.OkPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.parser.ServerParseSet;
import com.alibaba.cobar.server.response.CharacterSet;
import com.dianping.bee.engine.spi.SessionManager;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SetHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(SetHandler.class);

	@Inject
	private SessionManager m_sessionManager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
	}

	private static final byte[] AC_OFF = new byte[] { 7, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0 };

	@SuppressWarnings("unchecked")
   public void handle(String stmt, ServerConnection c, int offset) {
		LOGGER.info("set : " + stmt);
		int rs = ServerParseSet.parse(stmt, offset);
		Map<String, Object> metadata = m_sessionManager.getSession().getMetadata();
		switch (rs & 0xff) {
		case AUTOCOMMIT_ON:
			if (c.isAutocommit()) {
				c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			} else {
				c.commit();
				c.setAutocommit(true);
			}
			break;
		case AUTOCOMMIT_OFF: {
			if (c.isAutocommit()) {
				c.setAutocommit(false);
			}
			c.write(c.writeToBuffer(AC_OFF, c.allocate()));
			break;
		}
		case TX_READ_UNCOMMITTED: {
			c.setTxIsolation(Isolations.READ_UNCOMMITTED);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			metadata.put("tx_isolation", "READ_UNCOMMITTED");
			metadata.put("transaction_isolation", "READ_UNCOMMITTED");
			break;
		}
		case TX_READ_COMMITTED: {
			c.setTxIsolation(Isolations.READ_COMMITTED);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			metadata.put("tx_isolation", "READ_COMMITTED");
			metadata.put("transaction_isolation", "READ_COMMITTED");
			break;
		}
		case TX_REPEATED_READ: {
			c.setTxIsolation(Isolations.REPEATED_READ);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			metadata.put("tx_isolation", "REPEATED_READ");
			metadata.put("transaction_isolation", "REPEATED_READ");
			break;
		}
		case TX_SERIALIZABLE: {
			c.setTxIsolation(Isolations.SERIALIZABLE);
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
			metadata.put("tx_isolation", "SERIALIZABLE");
			metadata.put("transaction_isolation", "SERIALIZABLE");
			break;
		}
		case NAMES:
			String charset = stmt.substring(rs >>> 8).trim();
			if (c.setCharset(charset)) {
				c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
				((List<String>) metadata.get("character_set")).add(charset);
			} else {
				c.writeErrMessage(ErrorCode.ER_UNKNOWN_CHARACTER_SET, "Unknown charset '" + charset + "'");
			}
			break;
		case CHARACTER_SET_CLIENT:
		case CHARACTER_SET_CONNECTION:
		case CHARACTER_SET_RESULTS:
			// TODO return charset from session
			CharacterSet.response(stmt, c, rs);
			break;
		default:
			StringBuilder s = new StringBuilder();
			LOGGER.warn(s.append(c).append(stmt).append(" is not executed").toString());
			c.write(c.writeToBuffer(OkPacket.OK, c.allocate()));
		}
	}
}
