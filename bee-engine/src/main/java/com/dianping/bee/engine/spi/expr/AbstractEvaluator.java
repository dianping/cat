package com.dianping.bee.engine.spi.expr;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.dianping.bee.engine.spi.row.RowContext;
import com.site.lookup.ContainerHolder;

public abstract class AbstractEvaluator<S extends Expression, T> extends ContainerHolder implements Evaluator<S, T> {
	@SuppressWarnings("unchecked")
	protected <V> V eval(RowContext ctx, Expression child) {
		Evaluator<Expression, V> evaluator = lookup(Evaluator.class, child.getClass().getName());

		return (V) evaluator.evaluate(ctx, child);
	}
}
