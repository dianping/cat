package com.dianping.bee.engine.evaluator.function;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Sum;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class SumEvaluator extends AbstractEvaluator<Sum, Number> {
	public static final String ID = Sum.class.getName();

	private double m_sum;

	@Override
	public Number evaluate(RowContext ctx, Sum expr) {
		Expression first = expr.getArguments().get(0);
		Object val = eval(ctx, first);

		if (val != null) {
			Number value = (Number) val;

			m_sum += value.doubleValue();
		}

		return 0;
	}

	@Override
	public Object getAggregatedValue() {
		return m_sum;
	}

	@Override
	public Class<?> getResultType(Sum expr) {
		return Double.class;
	}

	@Override
	public boolean isAggregator() {
		return true;
	}
}
