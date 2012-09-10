package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.logical.LogicalAndExpression;
import com.alibaba.cobar.parser.util.ExprEvalUtils;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;

public class LogicalAndEvaluator extends AbstractEvaluator<LogicalAndExpression, Boolean> {
	public static final String ID = LogicalAndExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, LogicalAndExpression expr) {
		int len = expr.getArity();

		for (int i = 0; i < len; i++) {
			Expression operand = expr.getOperand(i);
			Object value = eval(ctx, operand);

			if (value == null) {
				return null;
			} else if (!ExprEvalUtils.obj2bool(value)) {
				return Boolean.FALSE;
			}
		}

		return Boolean.TRUE;
	}
}
