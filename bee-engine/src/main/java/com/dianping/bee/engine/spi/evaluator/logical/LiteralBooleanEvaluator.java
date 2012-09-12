package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralBoolean;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;

public class LiteralBooleanEvaluator extends AbstractEvaluator<LiteralBoolean, Boolean> {
	public static final String ID = LiteralBoolean.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, LiteralBoolean expr) {
		return expr.isTrue();
	}
}
