package com.dianping.bee.engine.evaluator.function;

import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Count;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class CountEvaluator extends AbstractEvaluator<Count, Integer> {
	public static final String ID = Count.class.getName();

	private int m_count;

	@Override
	public Integer evaluate(RowContext ctx, Count expr) {
		m_count++;
		return 0;
	}

	@Override
	public Object getAggregatedValue() {
		return m_count;
	}

	@Override
	public Class<?> getResultType(Count expr) {
		return Integer.class;
	}

	@Override
	public boolean isAggregator() {
		return true;
	}
}
