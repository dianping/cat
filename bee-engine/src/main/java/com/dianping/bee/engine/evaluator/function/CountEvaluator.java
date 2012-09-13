package com.dianping.bee.engine.evaluator.function;

import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Count;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class CountEvaluator extends AbstractEvaluator<Count, Long> {
	public static final String ID = Count.class.getName();

	private long m_count;

	@Override
	public Long evaluate(RowContext ctx, Count expr) {
		m_count++;
		return 0L;
	}

	@Override
	public Object getAggregatedValue() {
		return m_count;
	}

	@Override
	public Class<?> getResultType(Count expr) {
		return Long.class;
	}

	@Override
	public boolean isAggregator() {
		return true;
	}
}
