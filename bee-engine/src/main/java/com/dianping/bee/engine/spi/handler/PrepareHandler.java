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
package com.dianping.bee.engine.spi.handler;

import java.nio.ByteBuffer;
import java.sql.SQLSyntaxErrorException;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.util.BufferUtil;
import com.alibaba.cobar.net.util.PacketUtil;
import com.alibaba.cobar.protocol.MySQLPacket;
import com.alibaba.cobar.protocol.mysql.FieldPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.Row;
import com.dianping.bee.engine.RowSet;
import com.dianping.bee.engine.helper.TypeUtils;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.PreparedStatement;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import org.unidal.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class PrepareHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(PrepareHandler.class);

	@Inject
	private StatementManager m_manager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
	}

	/**
	 * @param stmtId
	 * @param parameters
	 * @param c
	 */
	public void execute(Long stmtId, List<Object> parameters, ServerConnection c) {
		LOGGER.info("execute : " + stmtId);
		PreparedStatement stmt = m_manager.getPreparedStatement(stmtId);
		int len = parameters.size();

		for (int i = 0; i < len; i++) {
			stmt.setParameter(i, parameters.get(i));
		}

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
			ctx.writeBinaryRow(row);
		}

		ctx.writeEOF();
		ctx.complete();
	}

	public PreparedStatement getStatement(Long stmtId) {
		return m_manager.getPreparedStatement(stmtId);
	}

	/**
	 * @param sql
	 * @param c
	 */
	public void prepare(String sql, ServerConnection c) {
		LOGGER.info("prepare : " + sql);
		Statement ori_stmt;

		try {
			ori_stmt = m_manager.build(sql);
		} catch (SQLSyntaxErrorException e) {
			error(c, ErrorCode.ER_SYNTAX_ERROR, e.getMessage());
			return;
		}

		PreparedStatement stmt = (PreparedStatement) ori_stmt;
		long stmtId = stmt.getStatementId();
		CommandContext ctx = new CommandContext(c);
		int columnSize = stmt.getColumnSize();
		int parameterSize = ((PreparedStatement) stmt).getParameterSize();

		PrepareOKPacket packet = new PrepareOKPacket(stmtId, columnSize, parameterSize);
		ctx.write(packet);

		for (int i = 0; i < parameterSize; i++) {
			ColumnMeta paramMeta = stmt.getParameterMeta(i);
			FieldPacket field = PacketUtil.getField(paramMeta.getName(),
			      TypeUtils.convertJavaTypeToFieldType(paramMeta.getType()));
			ctx.write(field);
		}
		ctx.writeEOF();

		for (int i = 0; i < columnSize; i++) {
			ColumnMeta colMeta = stmt.getColumnMeta(i);
			FieldPacket field = PacketUtil.getField(colMeta.getName(),
			      TypeUtils.convertJavaTypeToFieldType(colMeta.getType()));
			ctx.write(field);
		}
		ctx.writeEOF();

		ctx.complete();
	}

	static class PrepareOKPacket extends MySQLPacket {
		private byte m_status = 0;

		private byte m_reserved = 0;

		private long m_statementId;

		private int m_columnSize;

		private int m_parameterSize;

		private int m_warningCount;

		public PrepareOKPacket(long statementId, int columnSize, int parameterSize) {
			this.m_statementId = statementId;
			this.m_columnSize = columnSize;
			this.m_parameterSize = parameterSize;
		}

		@Override
		public int calcPacketSize() {
			return 12;// 1+4+2+2+1+2
		}

		@Override
		protected String getPacketInfo() {
			return "MySQL Prepare OK Packet";
		}

		@Override
		public ByteBuffer write(ByteBuffer buffer, FrontendConnection c) {
			int size = calcPacketSize();
			buffer = c.checkWriteBuffer(buffer, c.getPacketHeaderSize() + size);
			BufferUtil.writeUB3(buffer, size);
			buffer.put(packetId);
			buffer.put(m_status);
			BufferUtil.writeUB4(buffer, m_statementId);
			BufferUtil.writeUB2(buffer, m_columnSize);
			BufferUtil.writeUB2(buffer, m_parameterSize);
			buffer.put(m_reserved);
			BufferUtil.writeUB2(buffer, m_warningCount);
			return buffer;
		}
	}
}