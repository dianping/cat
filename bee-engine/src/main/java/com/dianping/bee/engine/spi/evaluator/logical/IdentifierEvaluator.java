package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;

public class IdentifierEvaluator extends AbstractEvaluator<Identifier, Object> {
	public static final String ID = Identifier.class.getName();

	@Override
	public Object evaluate(RowContext ctx, Identifier expr) {
		String id = expr.getIdText();
		Object value = ctx.getValue(id);

		return value;
	}
}
