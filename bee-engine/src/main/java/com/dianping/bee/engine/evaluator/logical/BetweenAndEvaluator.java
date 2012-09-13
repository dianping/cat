package com.dianping.bee.engine.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.comparison.BetweenAndExpression;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class BetweenAndEvaluator extends AbstractEvaluator<BetweenAndExpression, Boolean> {
	public static final String ID = BetweenAndExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, BetweenAndExpression expr) {
		boolean isNot = expr.isNot();
		Integer r1 = compareTo(ctx, expr.getFirst(), expr.getSecond());

		if (r1 == null) {
			return null;
		} else if (r1 < 0) {
			return isNot ? Boolean.TRUE : Boolean.FALSE;
		}

		Integer r2 = compareTo(ctx, expr.getFirst(), expr.getThird());

		if (r2 == null) {
			return null;
		} else if (r2 >= 0) {
			return isNot ? Boolean.TRUE : Boolean.FALSE;
		} else {
			return isNot ? Boolean.FALSE : Boolean.TRUE;
		}
	}
}
