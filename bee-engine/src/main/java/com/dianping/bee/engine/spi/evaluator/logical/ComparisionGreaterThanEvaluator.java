package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionGreaterThanExpression;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.row.RowContext;

public class ComparisionGreaterThanEvaluator extends AbstractEvaluator<ComparisionGreaterThanExpression, Boolean> {
	public static final String ID = ComparisionGreaterThanExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, ComparisionGreaterThanExpression expr) {
		Integer result = compareTo(ctx, expr.getLeftOprand(), expr.getRightOprand());

		if (result == null) {
			return null;
		} else if (result > 0) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}
