package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalOrExpression;
import com.alibaba.cobar.parser.util.ExprEvalUtils;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.row.RowContext;

public class LogicalOrEvaluator extends AbstractEvaluator<LogicalOrExpression, Boolean> {
	public static final String ID = LogicalOrExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, LogicalOrExpression expr) {
		int len = expr.getArity();

		for (int i = 0; i < len; i++) {
			Expression operand = expr.getOperand(i);
			Object value = eval(ctx, operand);

			if (value == null) {
				return null;
			} else if (ExprEvalUtils.obj2bool(value)) {
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}
}
