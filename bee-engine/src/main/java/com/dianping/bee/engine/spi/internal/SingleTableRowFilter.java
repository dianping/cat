package com.dianping.bee.engine.spi.internal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.dianping.bee.engine.spi.expr.Evaluator;
import com.dianping.bee.engine.spi.row.RowContext;
import com.dianping.bee.engine.spi.row.RowFilter;
import com.site.lookup.ContainerHolder;

public class SingleTableRowFilter extends ContainerHolder implements RowFilter {
	private Expression m_expr;

	@SuppressWarnings("unchecked")
	@Override
	public boolean filter(RowContext ctx) {
		Evaluator<Expression, Boolean> evaluator = lookup(Evaluator.class, m_expr.getClass().getName());
		Object value = evaluator.evaluate(ctx, m_expr);

		if (value == null) {
			return false;
		} else {
			return (Boolean) value;
		}
	}

	public SingleTableRowFilter setExpression(Expression expr) {
		m_expr = expr;
		return this;
	}
}
