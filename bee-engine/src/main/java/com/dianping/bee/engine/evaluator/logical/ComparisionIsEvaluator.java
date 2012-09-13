package com.dianping.bee.engine.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionIsExpression;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class ComparisionIsEvaluator extends AbstractEvaluator<ComparisionIsExpression, Boolean> {
	public static final String ID = ComparisionIsExpression.class.getName();

	@Override
	public Boolean evaluate(RowContext ctx, ComparisionIsExpression expr) {
		Expression operand = expr.getOperand();
		Object value = eval(ctx, operand);
		int mode = expr.getMode();

		if (value == null) {
			if (mode == ComparisionIsExpression.IS_NULL) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}

		switch (mode) {
		case ComparisionIsExpression.IS_NOT_NULL:
			return Boolean.TRUE;
		case ComparisionIsExpression.IS_TRUE:
			return "true".equalsIgnoreCase(value.toString());
		case ComparisionIsExpression.IS_NOT_TRUE:
			return !"true".equalsIgnoreCase(value.toString());
		case ComparisionIsExpression.IS_FALSE:
			return "false".equalsIgnoreCase(value.toString());
		case ComparisionIsExpression.IS_NOT_FALSE:
			return !"false".equalsIgnoreCase(value.toString());
		case ComparisionIsExpression.IS_UNKNOWN:
		case ComparisionIsExpression.IS_NOT_UNKNOWN:
		}

		return Boolean.FALSE;
	}
}
