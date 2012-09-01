package com.dianping.bee.engine.spi.expr.internal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.util.ExprEvalUtils;
import com.alibaba.cobar.parser.util.Pair;
import com.dianping.bee.engine.spi.expr.AbstractEvaluator;
import com.dianping.bee.engine.spi.row.RowContext;

public class ComparisionEqualsEvaluator extends AbstractEvaluator<ComparisionEqualsExpression, Boolean> {
	public static final String ID = ComparisionEqualsExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, ComparisionEqualsExpression expr) {
		Expression left = expr.getLeftOprand();
		Expression right = expr.getRightOprand();
		Object leftValue = eval(ctx, left);
		Object rightValue = eval(ctx, right);

		if (leftValue == null || rightValue == null) {
			return null;
		}

		if (left instanceof Number || right instanceof Number) {
			Pair<Number, Number> pair = ExprEvalUtils.convertNum2SameLevel(left, right);
			leftValue = pair.getKey();
			rightValue = pair.getValue();
		}

		return leftValue.equals(rightValue);
	}
}
