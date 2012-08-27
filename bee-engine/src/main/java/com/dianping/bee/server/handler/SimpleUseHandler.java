/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-24
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
package com.dianping.bee.server.handler;

import java.nio.ByteBuffer;
import java.util.Set;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendPrivileges;
import com.alibaba.cobar.protocol.mysql.OkPacket;
import com.alibaba.cobar.server.ServerConnection;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleUseHandler {
	public void handle(String sql, ServerConnection c, int offset) {
		String schema = sql.substring(offset).trim();
		int length = schema.length();
		if (length > 0) {
			if (schema.charAt(0) == '`' && schema.charAt(length - 1) == '`') {
				schema = schema.substring(1, length - 1);
			}
		}

		// 表示当前连接已经指定了schema
		// if (c.getSchema() != null) {
		// if (c.getSchema().equals(schema)) {
		// ByteBuffer buffer = c.allocate();
		// c.write(c.writeToBuffer(OkPacket.OK, buffer));
		// } else {
		// c.writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR,
		// "Not allowed to change the database!");
		// }
		// return;
		// }

		// 检查schema的有效性
		FrontendPrivileges privileges = c.getPrivileges();
		if (schema == null || !privileges.schemaExists(schema)) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + schema + "'");
			return;
		}
		String user = c.getUser();
		if (!privileges.userExists(user, c.getHost())) {
			c.writeErrMessage(ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '" + c.getUser() + "'");
			return;
		}
		Set<String> schemas = privileges.getUserSchemas(user);
		if (schemas == null || schemas.size() == 0 || schemas.contains(schema)) {
			c.setSchema(schema);
			ByteBuffer buffer = c.allocate();
			c.write(c.writeToBuffer(OkPacket.OK, buffer));
		} else {
			String msg = "Access denied for user '" + c.getUser() + "' to database '" + schema + "'";
			c.writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, msg);
		}
	}
}
