package com.dianping.bee.engine.evaluator.function;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Avg;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class AvgEvaluator extends AbstractEvaluator<Avg, Number> {
	public static final String ID = Avg.class.getName();

	private long m_count;

	private double m_sum;

	@Override
	public Number evaluate(RowContext ctx, Avg expr) {
		Expression first = expr.getArguments().get(0);
		Object val = eval(ctx, first);

		if (val != null) {
			m_sum += ((Number) val).doubleValue();
			m_count++;
		}
		return 0;
	}

	@Override
	public Object getAggregatedValue() {
		return m_sum / m_count;
	}

	@Override
	public Class<?> getResultType(Avg expr) {
		return Number.class;
	}

	@Override
	public boolean isAggregator() {
		return true;
	}
}
