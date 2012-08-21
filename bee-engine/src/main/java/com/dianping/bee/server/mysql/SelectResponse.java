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
import com.dianping.whale.engine.IQueryInterface;
import com.dianping.whale.server.WhaleServer;
import com.dianping.whale.storage.RowSet;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SelectResponse {

	private static final Logger LOGGER = Logger.getLogger(SelectResponse.class);

	private static final Map<String, IQueryInterface> queryInterfaces = new HashMap<String, IQueryInterface>();

	/**
	 * find Query Engine by schema
	 * 
	 * @param c
	 * @return
	 */
	private static IQueryInterface getQueryInterface(ServerConnection c) {
		String schema = c.getSchema();
		SchemaConfig schemaConfig = WhaleServer.getInstance().getConfig().getSchemas().get(schema);
		if (schemaConfig != null) {
			String dataNode = schemaConfig.getDataNode();
			String dataSourceName = WhaleServer.getInstance().getConfig().getDataNodes().get(dataNode).getSource()
			      .getName();
			DataSourceConfig dataSourceConfig = WhaleServer.getInstance().getConfig().getDataSources().get(dataSourceName);
			String type = dataSourceConfig.getType();
			if ("whale".equals(type)) {
				String classPath = dataSourceConfig.getSqlMode();
				IQueryInterface queryInstance = queryInterfaces.get(classPath);
				if (queryInstance == null) {
					try {
						queryInstance = (IQueryInterface) Class.forName(classPath).newInstance();
						queryInterfaces.put(classPath, queryInstance);
					} catch (Exception e) {
						LOGGER.warn("Instance Query Interface Failed", e);
						return null;
					}
				}
				return queryInstance;
			} else if ("mysql".equals(type)) {
			}
		}
		return null;
	}

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
	public static void response(ServerConnection c, String stmt) {
		IQueryInterface queryInterface = getQueryInterface(c);
		if (queryInterface == null) {
			LOGGER.warn("No Query Interface Found");
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, stmt);
			return;
		}

		RowSet result = null;
		try {
			result = queryInterface.query(stmt);
		} catch (SQLException e) {
			LOGGER.warn(stmt, e);
			c.writeErrMessage(ErrorCode.ER_PARSE_ERROR, stmt);
			return;
		}

		byte packetId = 0;
		EOFPacket eof = new EOFPacket();
		ByteBuffer buffer = c.allocate();
		// write header
		int fieldCount = result.getMetaData().getColumnCount();
		ResultSetHeaderPacket header = PacketUtil.getHeader(fieldCount);
		header.packetId = ++packetId;
		buffer = header.write(buffer, c);

		// write fields
		int columnIndex = 0;
		FieldPacket[] fields = new FieldPacket[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			fields[columnIndex] = PacketUtil.getField(result.getMetaData().getColumnNames()[i], result.getMetaData()
			      .getColumnTypes()[i]);
			fields[columnIndex++].packetId = ++packetId;
		}
		eof.packetId = ++packetId;

		for (FieldPacket field : fields) {
			buffer = field.write(buffer, c);
		}
		buffer = eof.write(buffer, c);

		// write rows
		for (int rowIndex = 0; rowIndex < result.getData().length; rowIndex++) {
			RowDataPacket row = getRow(result.getData()[rowIndex], result.getMetaData().getColumnTypes(), c.getCharset());
			row.packetId = ++packetId;
			buffer = row.write(buffer, c);
		}

		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c);
		c.write(buffer);
	}
}
