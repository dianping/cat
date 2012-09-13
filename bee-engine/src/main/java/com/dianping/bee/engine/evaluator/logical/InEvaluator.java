package com.dianping.bee.engine.evaluator.logical;

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.InExpression;
import com.alibaba.cobar.parser.ast.expression.misc.InExpressionList;
import com.alibaba.cobar.parser.ast.expression.misc.QueryExpression;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class InEvaluator extends AbstractEvaluator<InExpression, Boolean> {
	public static final String ID = InExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, InExpression expr) {
		boolean isNot = expr.isNot();
		Expression left = expr.getLeftOprand();
		Object leftValue = eval(ctx, left);

		if (leftValue == null) {
			return null;
		}

		InExpressionList inList = expr.getInExpressionList();

		if (inList != null) {
			List<Expression> list = inList.getList();

			for (Expression item : list) {
				Integer result = compareTo(ctx, left, item);

				if (result != null && result.intValue() == 0) {
					return isNot ? Boolean.FALSE : Boolean.TRUE;
				}
			}
		}

		QueryExpression inQuery = expr.getQueryExpression();

		if (inQuery != null) {
			throw new RuntimeException("In sub query is not implemented yet!");
		}

		return isNot ? Boolean.TRUE : Boolean.FALSE;
	}
}
