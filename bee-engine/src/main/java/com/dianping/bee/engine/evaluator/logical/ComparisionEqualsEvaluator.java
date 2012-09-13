package com.dianping.bee.engine.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class ComparisionEqualsEvaluator extends AbstractEvaluator<ComparisionEqualsExpression, Boolean> {
	public static final String ID = ComparisionEqualsExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, ComparisionEqualsExpression expr) {
		Integer result = compareTo(ctx, expr.getLeftOprand(), expr.getRightOprand());

		if (result == null) {
			return null;
		} else if (result.intValue() == 0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}
