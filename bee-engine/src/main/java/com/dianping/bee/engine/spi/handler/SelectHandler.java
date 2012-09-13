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
package com.dianping.bee.engine.spi.handler;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.response.SelectDatabase;
import com.alibaba.cobar.server.response.SelectUser;
import com.alibaba.cobar.server.response.SelectVersion;
import com.alibaba.cobar.server.response.SelectVersionComment;
import com.dianping.bee.engine.Row;
import com.dianping.bee.engine.RowSet;
import com.dianping.bee.engine.helper.TypeUtils;
import com.dianping.bee.engine.spi.SessionManager;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.StatementManager;
import com.site.helper.Joiners;
import com.site.lookup.annotation.Inject;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SelectHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(SelectHandler.class);

	@Inject
	private StatementManager m_manager;

	@Inject
	private SessionManager m_sessionManager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
		int len = parts.size();
		String first = len > 0 ? parts.get(0) : null;

		if ("@@VERSION_COMMENT".equalsIgnoreCase(first)) {
			SelectVersionComment.response(c);
		} else if ("DATABASE".equalsIgnoreCase(first)) {
			SelectDatabase.response(c);
		} else if ("DATABASE()".equalsIgnoreCase(first)) {
			selectCurrentDatabase(c);
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

	/**
	 * @param c
	 */
	private void selectCurrentDatabase(ServerConnection c) {
		LOGGER.info("selectCurrentDatabase");
		CommandContext ctx = new CommandContext(c);
		String[] names = { "database()" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
		}

		ctx.writeEOF();

		String schema = c.getSchema();
		ctx.writeRow(schema);

		ctx.writeEOF();
		ctx.complete();
	}

	/**
	 * @param c
	 * @param string
	 */
	private void selectSession(ServerConnection c, String sessionVariable) {
		LOGGER.info("selectSession : " + sessionVariable);
		CommandContext ctx = new CommandContext(c);
		String[] names = { sessionVariable };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
		}

		ctx.writeEOF();

		Map<String, Object> metadata = m_sessionManager.getSession().getMetadata();
		if (metadata.containsKey(sessionVariable)) {
			ctx.writeRow(String.valueOf(metadata.get(sessionVariable)));
		}

		ctx.writeEOF();
		ctx.complete();
	}

	/**
	 * @param c
	 * @param sql
	 * @throws SQLSyntaxErrorException
	 */
	private void selectStatement(ServerConnection c, String sql) throws SQLSyntaxErrorException {
		LOGGER.info("select : " + sql);
		if (c.getSchema() == null) {
			error(c, ErrorCode.ER_BAD_DB_ERROR, "No database selected");
			return;
		}

		Statement stmt = m_manager.build(sql);
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

}
