package com.dianping.bee.engine.evaluator.function;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Wildcard;
import com.alibaba.cobar.parser.ast.expression.primary.function.groupby.Count;
import com.dianping.bee.engine.evaluator.AbstractEvaluator;
import com.dianping.bee.engine.spi.RowContext;

public class CountEvaluator extends AbstractEvaluator<Count, Number> {
	public static final String ID = Count.class.getName();

	private long m_count;

	private Set<Object> m_distincts;

	@Override
	public Number evaluate(RowContext ctx, Count expr) {
		if (expr.isDistinct()) {
			if (m_distincts == null) {
				m_distincts = new HashSet<Object>();
			}

			Expression first = expr.getArguments().get(0);

			if (first instanceof Wildcard) {
				m_count++;
			} else {
				Object val = eval(ctx, first);

				m_distincts.add(val);
			}
		} else {
			m_count++;
		}

		return 0;
	}

	@Override
	public Object getAggregatedValue() {
		if (m_distincts != null) {
			return m_distincts.size();
		} else {
			return m_count;
		}
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
