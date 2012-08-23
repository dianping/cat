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

import java.nio.ByteBuffer;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.net.util.PacketUtil;
import com.alibaba.cobar.protocol.mysql.EOFPacket;
import com.alibaba.cobar.protocol.mysql.FieldPacket;
import com.alibaba.cobar.protocol.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.protocol.mysql.RowDataPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.parser.ServerParse;
import com.alibaba.cobar.server.response.ShowDatabases;
import com.alibaba.cobar.util.StringUtil;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.site.lookup.ContainerLoader;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleShowHandler {

	/**
	 * @param stmt
	 * @param c
	 * @param offset
	 */
	public void handle(String stmt, ServerConnection c, int offset) {
		switch (SimpleServerParseShow.parse(stmt, offset)) {
		case SimpleServerParseShow.DATABASES:
			ShowDatabases.response(c);
			break;
		case SimpleServerParseShow.TABLES:
			showTable(c, stmt, ServerParse.SHOW);
			break;
		default:
			c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported show command");
		}
	}

	private void showTable(ServerConnection c, String sql, int type) {
		// 检查当前使用的DB
		String db = c.getSchema();
		if (db == null) {
			c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
			return;
		}
		SchemaConfig schema = CobarServer.getInstance().getConfig().getSchemas().get(db);
		if (schema == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
			return;
		}

		DatabaseProvider provider = null;
		try {
			provider = ContainerLoader.getDefaultContainer().lookup(DatabaseProvider.class, db);
		} catch (ComponentLookupException e) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Can not load database '" + db + "'");
			return;
		}
		if (provider == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Can not load database '" + db + "'");
			return;
		}

		TableProvider[] tables = provider.getTables();

		int FIELD_COUNT = 1;
		ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
		FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
		EOFPacket eof = new EOFPacket();
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;
		fields[i] = PacketUtil.getField("TABLE", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		eof.packetId = ++packetId;

		ByteBuffer buffer = c.allocate();

		// write header
		buffer = header.write(buffer, c);

		// write fields
		for (FieldPacket field : fields) {
			buffer = field.write(buffer, c);
		}

		// write eof
		buffer = eof.write(buffer, c);

		// write rows
		packetId = eof.packetId;

		for (TableProvider table : tables) {
			RowDataPacket row = new RowDataPacket(FIELD_COUNT);
			row.add(StringUtil.encode(table.getName(), c.getCharset()));
			row.packetId = ++packetId;
			buffer = row.write(buffer, c);
		}

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c);

		// post write
		c.write(buffer);
	}
}
