package com.dianping.bee.engine.evaluator.function;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Max;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class MaxEvaluator extends AbstractEvaluator<Max, Number> {
	public static final String ID = Max.class.getName();

	private double m_max = Double.MIN_VALUE;

	@Override
	public Number evaluate(RowContext ctx, Max expr) {
		Expression first = expr.getArguments().get(0);
		Object val = eval(ctx, first);

		if (val != null && val instanceof Number) {
			if (((Number) val).doubleValue() > m_max) {
				m_max = ((Number) val).doubleValue();
			}
		}

		return 0;
	}

	@Override
	public Object getAggregatedValue() {
		return m_max;
	}

	@Override
	public Class<?> getResultType(Max expr) {
		return Number.class;
	}

	@Override
	public boolean isAggregator() {
		return true;
	}
}
