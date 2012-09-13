package com.dianping.bee.engine.evaluator.literal;

import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class LiteralNumberEvaluator extends AbstractEvaluator<LiteralNumber, Number> {
	public static final String ID = LiteralNumber.class.getName();

	@Override
	public Number evaluate(RowContext ctx, LiteralNumber expr) {
		Number value = expr.getNumber();

		return value;
	}

	@Override
   public Class<?> getResultType(LiteralNumber expr) {
		 return Number.class; // TODO should be int or long, double etc.?
   }
}
