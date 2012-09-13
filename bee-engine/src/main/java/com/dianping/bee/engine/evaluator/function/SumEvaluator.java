package com.dianping.bee.engine.evaluator.function;

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Sum;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class SumEvaluator extends AbstractEvaluator<Sum, String> {
	public static final String ID = Sum.class.getName();

	@Override
	public String evaluate(RowContext ctx, Sum expr) {
		List<Expression> args = expr.getArguments();
		StringBuilder sb = new StringBuilder();

		for (Expression arg : args) {
			Object val = eval(ctx, arg);

			if (val != null) {
				sb.append(val);
			}
		}

		return sb.toString();
	}
}
