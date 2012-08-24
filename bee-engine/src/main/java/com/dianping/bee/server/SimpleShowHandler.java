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
import java.util.HashMap;
import java.util.Map;

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
import com.alibaba.cobar.server.response.ShowDatabases;
import com.alibaba.cobar.util.StringUtil;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.site.lookup.ContainerLoader;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleShowHandler {
	@Inject
	private TableProviderManager m_manager;

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
			showTables(c, stmt);
			break;
		case SimpleServerParseShow.TABLESTATUS:
			showTableStatus(c, stmt);
			break;
		case SimpleServerParseShow.STATUS:
			showStatus(c, stmt);
			break;
		case SimpleServerParseShow.VARIABLES:
			showVariables(c, stmt);
			break;
		default:
			c.writeErrMessage(ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported show command");
		}
	}

	/**
	 * @param c
	 * @param stmt
	 */
	private void showTableStatus(ServerConnection c, String stmt) {
		String dbName = stmt.substring("show table status from ".length()).trim();

		DatabaseProvider provider = null;
		try {
			provider = ContainerLoader.getDefaultContainer().lookup(DatabaseProvider.class, dbName);
		} catch (ComponentLookupException e) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Can not load database '" + dbName + "'");
			return;
		}

		int FIELD_COUNT = 18;
		ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
		FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
		EOFPacket eof = new EOFPacket();
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;
		fields[i] = PacketUtil.getField("Name", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Engine", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Version", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Row_format", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Rows", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Avg_row_length", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Data_length", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Max_data_length", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Index_length", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Data_free", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Auto_increment", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Create_time", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Update_time", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Check_time", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Collation", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Checksum", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Create_options", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Comment", Fields.FIELD_TYPE_VAR_STRING);
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

		// TODO: sample result currently
		TableProvider[] tables = provider.getTables();
		if (tables != null) {
			for (TableProvider table : tables) {
				RowDataPacket row = new RowDataPacket(FIELD_COUNT);
				row.add(StringUtil.encode(table.getName(), c.getCharset()));
				row.add(StringUtil.encode("Bee", c.getCharset()));
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.add(null);
				row.packetId = ++packetId;
				buffer = row.write(buffer, c);
			}
		}

		// write last eof
		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c);

		// post write
		c.write(buffer);
	}

	/**
	 * @param c
	 * @param stmt
	 */
	private void showStatus(ServerConnection c, String stmt) {
		int FIELD_COUNT = 2;
		ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
		FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
		EOFPacket eof = new EOFPacket();
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;
		fields[i] = PacketUtil.getField("Variable_name", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Value", Fields.FIELD_TYPE_VAR_STRING);
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

		// TODO: sample result currently
		Map<String, String> sampleStatus = new HashMap<String, String>();
		sampleStatus.put("bee_status", "good");
		for (Map.Entry<String, String> variable : sampleStatus.entrySet()) {
			RowDataPacket row = new RowDataPacket(FIELD_COUNT);
			row.add(StringUtil.encode(variable.getKey(), c.getCharset()));
			row.add(StringUtil.encode(variable.getValue(), c.getCharset()));
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

	/**
	 * 
	 * @param c
	 * @param stmt
	 */
	private void showTables(ServerConnection c, String stmt) {
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

		if (tables != null) {
			for (TableProvider table : tables) {
				RowDataPacket row = new RowDataPacket(FIELD_COUNT);
				row.add(StringUtil.encode(table.getName(), c.getCharset()));
				row.packetId = ++packetId;
				buffer = row.write(buffer, c);
			}
		} else {
			RowDataPacket row = new RowDataPacket(FIELD_COUNT);
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

	/**
	 * @param c
	 * @param stmt
	 */
	private void showVariables(ServerConnection c, String stmt) {
		int FIELD_COUNT = 2;
		ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
		FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
		EOFPacket eof = new EOFPacket();
		int i = 0;
		byte packetId = 0;
		header.packetId = ++packetId;
		fields[i] = PacketUtil.getField("Variable_name", Fields.FIELD_TYPE_VAR_STRING);
		fields[i++].packetId = ++packetId;
		fields[i] = PacketUtil.getField("Value", Fields.FIELD_TYPE_VAR_STRING);
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

		// TODO: sample result currently
		Map<String, String> sampleVariables = new HashMap<String, String>();
		sampleVariables.put("bee_status", "good");
		for (Map.Entry<String, String> variable : sampleVariables.entrySet()) {
			RowDataPacket row = new RowDataPacket(FIELD_COUNT);
			row.add(StringUtil.encode(variable.getKey(), c.getCharset()));
			row.add(StringUtil.encode(variable.getValue(), c.getCharset()));
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
