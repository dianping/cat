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
package com.dianping.bee.server;

import java.nio.ByteBuffer;
import java.sql.SQLSyntaxErrorException;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.net.util.PacketUtil;
import com.alibaba.cobar.parser.util.ParseUtil;
import com.alibaba.cobar.protocol.mysql.EOFPacket;
import com.alibaba.cobar.protocol.mysql.FieldPacket;
import com.alibaba.cobar.protocol.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.protocol.mysql.RowDataPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.parser.ServerParseSelect;
import com.alibaba.cobar.server.response.SelectDatabase;
import com.alibaba.cobar.server.response.SelectIdentity;
import com.alibaba.cobar.server.response.SelectLastInsertId;
import com.alibaba.cobar.server.response.SelectUser;
import com.alibaba.cobar.server.response.SelectVersion;
import com.alibaba.cobar.server.response.SelectVersionComment;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.StringUtil;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.meta.internal.TypeUtils;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleSelectHandler {

	@Inject
	private StatementManager m_manager;

	private RowDataPacket getRow(RowSet rowset, int rowIndex, String charset) {
		int cols = rowset.getColumns();
		RowDataPacket packet = new RowDataPacket(cols);
		Row row = rowset.getRow(rowIndex);

		for (int i = 0; i < cols; i++) {
			ColumnMeta column = rowset.getColumn(i);
			Cell cell = row.getCell(i);
			String value = cell.getValue().toString();
			switch (TypeUtils.convertJavaTypeToFieldType(column.getType())) {
			case Fields.FIELD_TYPE_STRING:
				packet.add(StringUtil.encode(value, charset));
				break;
			case Fields.FIELD_TYPE_INT24:
				packet.add(value == null ? null : IntegerUtil.toBytes(Integer.parseInt(value)));
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
				packet.add(StringUtil.encode(value, charset));
			}

		}
		return packet;
	}

	public void handle(String stmt, ServerConnection c, int offs) {
		int offset = offs;
		switch (ServerParseSelect.parse(stmt, offs)) {
		case ServerParseSelect.VERSION_COMMENT:
			SelectVersionComment.response(c);
			break;
		case ServerParseSelect.DATABASE:
			SelectDatabase.response(c);
			break;
		case ServerParseSelect.USER:
			SelectUser.response(c);
			break;
		case ServerParseSelect.VERSION:
			SelectVersion.response(c);
			break;
		case ServerParseSelect.LAST_INSERT_ID:
			// offset = ParseUtil.move(stmt, 0, "select".length());
			loop: for (; offset < stmt.length(); ++offset) {
				switch (stmt.charAt(offset)) {
				case ' ':
					continue;
				case '/':
				case '#':
					offset = ParseUtil.comment(stmt, offset);
					continue;
				case 'L':
				case 'l':
					break loop;
				}
			}
			offset = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, offset);
			offset = ServerParseSelect.skipAs(stmt, offset);
			SelectLastInsertId.response(c, stmt, offset);
			break;
		case ServerParseSelect.IDENTITY:
			// offset = ParseUtil.move(stmt, 0, "select".length());
			loop: for (; offset < stmt.length(); ++offset) {
				switch (stmt.charAt(offset)) {
				case ' ':
					continue;
				case '/':
				case '#':
					offset = ParseUtil.comment(stmt, offset);
					continue;
				case '@':
					break loop;
				}
			}
			int indexOfAtAt = offset;
			offset += 2;
			offset = ServerParseSelect.indexAfterIdentity(stmt, offset);
			String orgName = stmt.substring(indexOfAtAt, offset);
			offset = ServerParseSelect.skipAs(stmt, offset);
			SelectIdentity.response(c, stmt, offset, orgName);
			break;
		default:
			try {
				response(c, stmt);
			} catch (SQLSyntaxErrorException e) {
				c.writeErrMessage(ErrorCode.ER_SYNTAX_ERROR, e.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param c
	 * @param stmt
	 * @throws SQLSyntaxErrorException
	 */
	public void response(ServerConnection c, String sql) throws SQLSyntaxErrorException {
		Statement stmt = m_manager.build(sql);

		RowSet rowset = stmt.query();

		byte packetId = 0;
		EOFPacket eof = new EOFPacket();
		ByteBuffer buffer = c.allocate();
		// write header
		int fieldCount = rowset.getColumns();
		ResultSetHeaderPacket header = PacketUtil.getHeader(fieldCount);
		header.packetId = ++packetId;
		buffer = header.write(buffer, c);

		// write fields
		int columnIndex = 0;
		FieldPacket[] fields = new FieldPacket[fieldCount];
		for (int i = 0; i < fieldCount; i++) {
			fields[columnIndex] = PacketUtil.getField(rowset.getColumn(i).getName(),
			      TypeUtils.convertJavaTypeToFieldType(rowset.getColumn(i).getType()));
			fields[columnIndex++].packetId = ++packetId;
		}
		eof.packetId = ++packetId;

		for (FieldPacket field : fields) {
			buffer = field.write(buffer, c);
		}
		buffer = eof.write(buffer, c);

		// write rows
		for (int rowIndex = 0; rowIndex < rowset.getRows(); rowIndex++) {
			RowDataPacket row = getRow(rowset, rowIndex, c.getCharset());

			row.packetId = ++packetId;
			buffer = row.write(buffer, c);
		}

		EOFPacket lastEof = new EOFPacket();
		lastEof.packetId = ++packetId;
		buffer = lastEof.write(buffer, c);
		c.write(buffer);
	}
}
