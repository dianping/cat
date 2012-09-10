package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;

public class LiteralStringEvaluator extends AbstractEvaluator<LiteralString, String> {
	public static final String ID = LiteralString.class.getName();

	@Override
	public String evaluate(RowContext ctx, LiteralString expr) {
		String value = expr.getString();

		return value;
	}
}
