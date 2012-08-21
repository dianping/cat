/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-17
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

import java.nio.channels.SocketChannel;
import java.util.Set;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.util.MySQLMessage;
import com.alibaba.cobar.protocol.mysql.OkPacket;
import com.alibaba.cobar.server.ServerConnection;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class WhaleServerConnection extends ServerConnection {

	/**
	 * @param channel
	 */
	public WhaleServerConnection(SocketChannel channel) {
		super(channel);
	}

	 public void initDB(byte[] data) {
       MySQLMessage mm = new MySQLMessage(data);
       mm.position(5);
       String db = mm.readString();

       // 检查schema是否已经设置
//       if (schema != null) {
//           if (schema.equals(db)) {
//               write(writeToBuffer(OkPacket.OK, allocate()));
//           } else {
//               writeErrMessage(ErrorCode.ER_DBACCESS_DENIED_ERROR, "Not allowed to change the database!");
//           }
//           return;
//       }

       // 检查schema的有效性
       if (db == null || !privileges.schemaExists(db)) {
           writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
           return;
       }
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
}
