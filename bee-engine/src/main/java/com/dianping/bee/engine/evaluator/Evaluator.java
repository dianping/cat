package com.dianping.bee.engine.evaluator;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.dianping.bee.engine.spi.RowContext;

public interface Evaluator<S extends Expression, T> {
	public T evaluate(RowContext ctx, S expr);

	public Class<?> getResultType(S expr);
}
