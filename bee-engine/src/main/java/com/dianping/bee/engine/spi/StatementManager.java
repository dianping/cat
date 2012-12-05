package com.dianping.bee.engine.spi;

import java.sql.SQLSyntaxErrorException;

public interface StatementManager {

	public Statement build(String sql) throws SQLSyntaxErrorException;

	public PreparedStatement getPreparedStatement(Long stmtId);
}
