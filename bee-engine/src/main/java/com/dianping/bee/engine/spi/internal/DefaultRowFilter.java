package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;
import com.dianping.bee.engine.spi.RowFilter;

public class DefaultRowFilter implements RowFilter {
	private Expression m_expr;

	@Override
	public boolean filter(List<Object> values) {
		return false;
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
