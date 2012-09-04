package com.dianping.bee.engine.spi;

import java.sql.SQLSyntaxErrorException;

public interface StatementManager {
	public Statement parseSQL(String sql) throws SQLSyntaxErrorException;

	public Statement build(String sql) throws SQLSyntaxErrorException;

	public long stmtPrepare(PreparedStatement stmt);

	public void stmtClose(long stmtId);

	public PreparedStatement getStatement(Long stmtId);
}
