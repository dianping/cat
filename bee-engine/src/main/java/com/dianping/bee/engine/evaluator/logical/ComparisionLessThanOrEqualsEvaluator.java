package com.dianping.bee.engine.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionLessThanOrEqualsExpression;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

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

	@Override
   public Class<?> getResultType(ComparisionLessThanOrEqualsExpression expr) {
		 return Boolean.class;
   }
}
