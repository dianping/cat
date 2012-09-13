package com.dianping.bee.engine.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionGreaterThanOrEqualsExpression;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class ComparisionGreaterThanOrEqualsEvaluator extends AbstractEvaluator<ComparisionGreaterThanOrEqualsExpression, Boolean> {
	public static final String ID = ComparisionGreaterThanOrEqualsExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, ComparisionGreaterThanOrEqualsExpression expr) {
		Integer result = compareTo(ctx, expr.getLeftOprand(), expr.getRightOprand());

		if (result == null) {
			return null;
		} else if (result >= 0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}
