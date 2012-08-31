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
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.dianping.bee.engine.spi.handler.AbstractCommandHandler;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class PrepareHandler extends AbstractCommandHandler {
	@Inject
	private StatementManager m_manager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
		// String stmt = Joiners.by(' ').join(parts);
		// try {
		// m_manager.stmtPrepare(stmt);
		// } catch (SQLSyntaxErrorException e) {
		// error(c, ErrorCode.ER_SYNTAX_ERROR, e.getMessage());
		// }
	}

	/**
	 * @param sql
	 * @param c
	 * @param offset
	 */
	public void close(String sql, ServerConnection c, int offset) {

	}

	/**
	 * @param sql
	 * @param c
	 * @param offset
	 */
	public void execute(String sql, ServerConnection c, int offset) {

	}

	/**
	 * @param sql
	 * @param c
	 * @param offset
	 */
	public void prepare(String sql, ServerConnection c, int offset) {
		Statement stmt = null;
		try {
			stmt = m_manager.parseSQL(sql);
		} catch (SQLSyntaxErrorException e) {
			error(c, ErrorCode.ER_SYNTAX_ERROR, e.getMessage());
		}

		long stmtId = m_manager.stmtPrepare(stmt);
		CommandContext ctx = new CommandContext(c);
		int columnSize = stmt.getSelectColumns().length;
		int parameterSize = stmt.getParameterSize();

		PreparePacket packet = new PreparePacket(stmtId, columnSize, parameterSize);
		ctx.write(packet);

		// FIXME: just some sample code here
		for (int i = 0; i < parameterSize; i++) {
			PrepareParameterPacket parameterPacket = new PrepareParameterPacket(Fields.FIELD_TYPE_STRING,
			      Fields.NOT_NULL_FLAG, (byte) 0, 50);
			ctx.write(parameterPacket);
		}

		ctx.complete();
	}
}