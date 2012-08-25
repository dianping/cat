package com.dianping.bee.engine.spi.internal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.meta.Row;

public class DefaultRowFilter implements RowFilter {
	private Expression m_expr;

	/**
	 * Return true if the row passed the filter
	 */
	@Override
	public boolean filter(Row row) {
		return true;
	}

	@Override
	public void setExpression(Expression expr) {
		m_expr = expr;
	}

	public String toString() {
		MySQLOutputASTVisitor visitor = new MySQLOutputASTVisitor(new StringBuilder());

		m_expr.accept(visitor);
		return visitor.getSql();
	}
}
