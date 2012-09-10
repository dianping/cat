package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionLessThanOrEqualsExpression;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;

public class ComparisionLessThanOrEqualsEvaluator extends AbstractEvaluator<ComparisionLessThanOrEqualsExpression, Boolean> {
	public static final String ID = ComparisionLessThanOrEqualsExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, ComparisionLessThanOrEqualsExpression expr) {
		Integer result = compareTo(ctx, expr.getLeftOprand(), expr.getRightOprand());

		if (result == null) {
			return null;
		} else if (result <= 0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}
