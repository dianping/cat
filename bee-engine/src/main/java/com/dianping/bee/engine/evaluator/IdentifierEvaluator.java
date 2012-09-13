package com.dianping.bee.engine.evaluator;

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.dianping.bee.engine.spi.RowContext;

public class IdentifierEvaluator extends AbstractEvaluator<Identifier, Object> {
	public static final String ID = Identifier.class.getName();

	@Override
	public Object evaluate(RowContext ctx, Identifier expr) {
		String id = expr.getIdText();
		Object value = ctx.getValue(id);

		return value;
	}

	@Override
	public Class<?> getResultType(Identifier expr) {
		throw new UnsupportedOperationException("This method should not be called!");
	}
}
