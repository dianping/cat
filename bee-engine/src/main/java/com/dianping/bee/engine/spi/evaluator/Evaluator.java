package com.dianping.bee.engine.spi.evaluator;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.dianping.bee.engine.spi.row.RowContext;

public interface Evaluator<S extends Expression, T> {
	public T evaluate(RowContext ctx, S expr);
}
