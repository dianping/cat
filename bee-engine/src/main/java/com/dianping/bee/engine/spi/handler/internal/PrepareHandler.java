/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-29
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
import com.alibaba.cobar.protocol.mysql.FieldPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.handler.AbstractCommandHandler;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.meta.internal.TypeUtils;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class PrepareHandler extends AbstractCommandHandler {
	@Inject
	private StatementManager m_manager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
	}

	/**
	 * @param stmtId
	 * @param c
	 */
	public void close(Long stmtId, ServerConnection c) {

	}

	/**
	 * @param stmtId
	 * @param parameters
	 * @param c
	 */
	public void execute(Long stmtId, List<Object> parameters, ServerConnection c) {
		PreparedStatement stmt = m_manager.getStatement(stmtId);
		stmt.setParameters(parameters);

		RowSet rowset = stmt.query();

		CommandContext ctx = new CommandContext(c);
		String[] names = new String[rowset.getColumnSize()];
		for (int colIndex = 0; colIndex < names.length; colIndex++) {
			names[colIndex] = rowset.getColumn(colIndex).getName();
		}

		ctx.writeHeader(names.length);

		for (int colIndex = 0; colIndex < names.length; colIndex++) {
			ctx.writeField(names[colIndex], TypeUtils.convertJavaTypeToFieldType(rowset.getColumn(colIndex).getType()));
		}

		ctx.writeEOF();

		for (int rowIndex = 0; rowIndex < rowset.getRowSize(); rowIndex++) {
			Row row = rowset.getRow(rowIndex);
			ctx.writeRow(row);
		}

		ctx.writeEOF();
		ctx.complete();
	}

	public PreparedStatement getStatement(Long stmtId) {
		return m_manager.getStatement(stmtId);
	}

	/**
	 * @param sql
	 * @param c
	 */
	public void prepare(String sql, ServerConnection c) {
		Statement stmt = null;
		try {
			stmt = m_manager.parseSQL(sql);
		} catch (SQLSyntaxErrorException e) {
			error(c, ErrorCode.ER_SYNTAX_ERROR, e.getMessage());
		}

		long stmtId = m_manager.stmtPrepare((PreparedStatement) stmt);
		CommandContext ctx = new CommandContext(c);
		int columnSize = stmt.getColumnSize();
		int parameterSize = ((PreparedStatement) stmt).getParameterSize();

		PreparePacket packet = new PreparePacket(stmtId, columnSize, parameterSize);
		ctx.write(packet);

		// FIXME: just some sample code here
		for (int i = 0; i < parameterSize; i++) {
			// PrepareParameterPacket parameterPacket = new
			// PrepareParameterPacket(Fields.FIELD_TYPE_STRING,
			// Fields.NOT_NULL_FLAG, (byte) 0, 50);
			FieldPacket field = new FieldPacket();
			field.type = Fields.FIELD_TYPE_STRING;
			field.flags = Fields.NOT_NULL_FLAG;
			field.decimals = (byte) 0;
			field.length = 50;
			// ctx.write(parameterPacket);
			ctx.write(field);
		}

		ctx.writeEOF();
		ctx.complete();
	}
}