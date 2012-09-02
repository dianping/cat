package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.row.RowContext;

public class LiteralNumberEvaluator extends AbstractEvaluator<LiteralNumber, Number> {
	public static final String ID = LiteralNumber.class.getName();

	@Override
	public Number evaluate(RowContext ctx, LiteralNumber expr) {
		Number value = expr.getNumber();

		return value;
	}
}
