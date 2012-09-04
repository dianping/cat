package com.dianping.bee.engine.spi;

import java.sql.SQLSyntaxErrorException;

public interface StatementManager {
	public Statement parseSQL(String sql) throws SQLSyntaxErrorException;

	public Statement build(String sql) throws SQLSyntaxErrorException;

	public long stmtPrepare(Statement stmt);

	public Statement stmtExecute(long stmtId);

	public void stmtClose(long stmtId);
}
