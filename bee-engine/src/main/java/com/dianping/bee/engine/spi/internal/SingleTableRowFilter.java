package com.dianping.bee.engine.spi.internal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.dianping.bee.engine.evaluator.Evaluator;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.RowFilter;
import org.unidal.lookup.ContainerHolder;

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
			return ((Boolean) value).booleanValue();
		}
	}

	public SingleTableRowFilter setExpression(Expression expr) {
		m_expr = expr;
		return this;
	}
}
