package com.dianping.bee.engine.evaluator.function;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Min;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class MinEvaluator extends AbstractEvaluator<Min, Number> {
	public static final String ID = Min.class.getName();

	private double m_min = Double.MAX_VALUE;

	@Override
	public Number evaluate(RowContext ctx, Min expr) {
		Expression first = expr.getArguments().get(0);
		Object val = eval(ctx, first);

		if (val != null && val instanceof Number) {
			if (((Number) val).doubleValue() < m_min) {
				m_min = ((Number) val).doubleValue();
			}
		}

		return 0;
	}

	@Override
	public Object getAggregatedValue() {
		return m_min;
	}

	@Override
	public Class<?> getResultType(Min expr) {
		return Number.class;
	}

	@Override
	public boolean isAggregator() {
		return true;
	}
}
