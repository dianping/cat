package com.dianping.bee.engine.spi.evaluator.function;

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.string.Concat;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;

public class ConcatEvaluator extends AbstractEvaluator<Concat, String> {
	public static final String ID = Concat.class.getName();

	@Override
	public String evaluate(RowContext ctx, Concat expr) {
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
