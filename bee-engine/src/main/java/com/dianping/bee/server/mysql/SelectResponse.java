/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-14
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
package com.dianping.bee.server.mysql;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.config.model.DataSourceConfig;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.net.util.PacketUtil;
import com.alibaba.cobar.protocol.mysql.EOFPacket;
import com.alibaba.cobar.protocol.mysql.FieldPacket;
import com.alibaba.cobar.protocol.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.protocol.mysql.RowDataPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.StringUtil;
import com.dianping.bee.engine.spi.RowSet;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SelectResponse {

	private static final Logger LOGGER = Logger.getLogger(SelectResponse.class);

	/**
	 * 
	 * @param rowData
	 * @param rowType
	 * @param charset
	 * @return
	 */
	private static RowDataPacket getRow(Object[] rowData, int[] rowType, String charset) {
		RowDataPacket row = new RowDataPacket(rowData.length);
		for (int i = 0; i < rowData.length; i++) {
			switch (rowType[i]) {
			case Fields.FIELD_TYPE_STRING:
				row.add(StringUtil.encode(rowData[i].toString(), charset));
				break;
			case Fields.FIELD_TYPE_INT24:
				row.add(IntegerUtil.toBytes(Integer.parseInt(rowData[i].toString())));
				break;
			case Fields.FIELD_TYPE_DECIMAL:
			case Fields.FIELD_TYPE_TINY:
			case Fields.FIELD_TYPE_SHORT:
			case Fields.FIELD_TYPE_LONG:
			case Fields.FIELD_TYPE_FLOAT:
			case Fields.FIELD_TYPE_DOUBLE:
			case Fields.FIELD_TYPE_NULL:
			case Fields.FIELD_TYPE_TIMESTAMP:
			case Fields.FIELD_TYPE_LONGLONG:
			case Fields.FIELD_TYPE_DATE:
			case Fields.FIELD_TYPE_TIME:
			case Fields.FIELD_TYPE_DATETIME:
			case Fields.FIELD_TYPE_YEAR:
			case Fields.FIELD_TYPE_NEWDATE:
			case Fields.FIELD_TYPE_VARCHAR:
			case Fields.FIELD_TYPE_BIT:
			case Fields.FIELD_TYPE_NEW_DECIMAL:
			case Fields.FIELD_TYPE_ENUM:
			case Fields.FIELD_TYPE_SET:
			case Fields.FIELD_TYPE_TINY_BLOB:
			case Fields.FIELD_TYPE_MEDIUM_BLOB:
			case Fields.FIELD_TYPE_LONG_BLOB:
			case Fields.FIELD_TYPE_BLOB:
			case Fields.FIELD_TYPE_VAR_STRING:
			case Fields.FIELD_TYPE_GEOMETRY:
			default:
				row.add(StringUtil.encode(rowData[i].toString(), charset));
			}
		}
		return row;
	}

	/**
	 * 
	 * @param c
	 * @param stmt
	 */
	public static void response(ServerConnection c, String sql) {
		StatementManager statementManager = lookup(StatementManager.class);
		Statement stmt = statementManager.parse(sql);

		RowSet result = stmt.query();

		byte packetId = 0;
		EOFPacket eof = new EOFPacket();
		ByteBuffer buffer = c.allocate();
		// write header
		int fieldCount = result.getColumns();
		ResultSetHeaderPacket header = PacketUtil.getHeader(fieldCount);
		header.packetId = ++packetId;
		buffer = header.write(buffer, c);

		// write fields
		int columnIndex = 0;
		FieldPacket[] fields = new FieldPacket[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			fields[columnIndex] = PacketUtil.getField(result.getColumn(i).getName(), result.getColumn(i).getType());
			fields[columnIndex++].packetId = ++packetId;
		}
		eof.packetId = ++packetId;

		for (FieldPacket field : fields) {
			buffer = field.write(buffer, c);
		}
		buffer = eof.write(buffer, c);

		// write rows
		for (int rowIndex = 0; rowIndex < result.getRows(); rowIndex++) {
			RowDataPacket row = getRow(result.getRow(rowIndex), result.getMetaData().getColumnTypes(), c.getCharset());
			row.packetId = ++packetId;
			buffer = row.write(buffer, c);
		}

		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c);
		c.write(buffer);
	}
}
