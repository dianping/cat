/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-27
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
package com.dianping.bee.engine.spi.handler.internal;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.protocol.mysql.RowDataPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.response.SelectDatabase;
import com.alibaba.cobar.server.response.SelectUser;
import com.alibaba.cobar.server.response.SelectVersion;
import com.alibaba.cobar.server.response.SelectVersionComment;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.StringUtil;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.handler.AbstractCommandHandler;
import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.meta.internal.TypeUtils;
import com.site.helper.Joiners;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SelectHandler extends AbstractCommandHandler {
	@Inject
	private StatementManager m_manager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
		int len = parts.size();
		String first = len > 0 ? parts.get(0) : null;

		if ("@@VERSION_COMMENT".equalsIgnoreCase(first)) {
			SelectVersionComment.response(c);
		} else if ("DATABASE".equalsIgnoreCase(first)) {
			SelectDatabase.response(c);
		} else if ("USER".equalsIgnoreCase(first)) {
			SelectUser.response(c);
		} else if ("VERSION".equalsIgnoreCase(first)) {
			SelectVersion.response(c);
		} else if (first != null && first.toUpperCase().startsWith("@@SESSION")) {
			selectSession(c, first.substring("@@SESSION.".length()));
		} else {
			try {
				String stmt = "select " + Joiners.by(' ').join(parts);
				selectStatement(c, stmt);
			} catch (SQLSyntaxErrorException e) {
				error(c, ErrorCode.ER_SYNTAX_ERROR, e.getMessage());
			}
		}
	}

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

	/**
	 * @param c
	 * @param sql
	 * @throws SQLSyntaxErrorException
	 */
	private void selectStatement(ServerConnection c, String sql) throws SQLSyntaxErrorException {
		Statement stmt = m_manager.build(sql);

		RowSet rowset = stmt.query();

		CommandContext ctx = new CommandContext(c);
		String[] names = new String[rowset.getColumns()];
		for (int colIndex = 0; colIndex < names.length; colIndex++) {
			names[colIndex] = rowset.getColumn(colIndex).getName();
		}

		ctx.writeHeader(names.length);

		for (int colIndex = 0; colIndex < names.length; colIndex++) {
			ctx.writeField(names[colIndex], TypeUtils.convertJavaTypeToFieldType(rowset.getColumn(colIndex).getType()));
		}

		ctx.writeEOF();

		for (int rowIndex = 0; rowIndex < rowset.getRows(); rowIndex++) {
			RowDataPacket row = getRow(rowset, rowIndex, c.getCharset());
			ctx.write(row);
		}

		ctx.writeEOF();
		ctx.complete();
	}

	/**
	 * @param c
	 * @param string
	 */
	private void selectSession(ServerConnection c, String sessionVariable) {
		CommandContext ctx = new CommandContext(c);
		String[] names = { "sessionVariable" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
		}

		ctx.writeEOF();

		// TODO real data here
		ctx.writeRow("1");

		ctx.writeEOF();
		ctx.complete();
	}

}
