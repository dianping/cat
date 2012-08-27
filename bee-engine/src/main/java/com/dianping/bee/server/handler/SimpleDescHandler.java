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
package com.dianping.bee.server.handler;

import java.nio.ByteBuffer;

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
import com.alibaba.cobar.util.StringUtil;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.internal.TypeUtils;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleDescHandler {
	@Inject
	private TableProviderManager m_manager;

	/**
	 * @param stmt
	 * @param c
	 * @param offset
	 */
	public void handle(String stmt, ServerConnection c, int offset) {
		String tableName = stmt.substring(offset).trim();
		int length = tableName.length();
		if (length > 0) {
			if (tableName.charAt(0) == '`' && tableName.charAt(length - 1) == '`') {
				tableName = tableName.substring(1, length - 1);
			}
		}
		
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

		TableProvider table = m_manager.getTableProvider(tableName);
		if (table == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_TABLE_ERROR, "Unknown table '" + tableName + "'");
			return;
		}

		ColumnMeta[] columns = table.getColumns();

		int FIELD_COUNT = 6;
		ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
		FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
		EOFPacket eof = new EOFPacket();
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;
		fields[i] = PacketUtil.getField("Field", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Type", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Null", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Key", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Default", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Extra", Fields.FIELD_TYPE_VAR_STRING);
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

		if (columns != null) {
			for (ColumnMeta column : columns) {
				RowDataPacket row = new RowDataPacket(FIELD_COUNT);
				row.add(StringUtil.encode(column.getName(), c.getCharset()));
				row.add(StringUtil.encode(
				      TypeUtils.convertFieldTypeToString(TypeUtils.convertJavaTypeToFieldType(column.getType())),
				      c.getCharset()));
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.packetId = ++packetId;
				buffer = row.write(buffer, c);
			}
		} else {
			RowDataPacket row = new RowDataPacket(FIELD_COUNT);
			row.add(null);
			row.add(null);
			row.add(null);
			row.add(null);
			row.add(null);
			row.add(null);
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
