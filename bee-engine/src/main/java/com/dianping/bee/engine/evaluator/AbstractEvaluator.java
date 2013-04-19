package com.dianping.bee.engine.evaluator;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.util.ExprEvalUtils;
import com.alibaba.cobar.parser.util.Pair;
import com.dianping.bee.engine.spi.RowContext;
import org.unidal.lookup.ContainerHolder;

public abstract class AbstractEvaluator<S extends Expression, T> extends ContainerHolder implements Evaluator<S, T> {
	protected double compareNumber(Number n1, Number n2) {
		Pair<Number, Number> pair = ExprEvalUtils.convertNum2SameLevel(n1, n2);
		Number v1 = pair.getKey();
		Number v2 = pair.getValue();

		return v1.doubleValue() - v2.doubleValue();
	}

	protected Integer compareTo(RowContext ctx, Expression left, Expression right) {
		Object v1 = eval(ctx, left);
		Object v2 = eval(ctx, right);

		if (v1 == null || v2 == null) {
			return null;
		}

		if (v1 instanceof String) {
			return ((String) v1).compareTo(String.valueOf(v2));
		}

		if (v1 instanceof Number) {
			Number n1 = (Number) v1;
			Number n2 = ExprEvalUtils.string2Number(String.valueOf(v2));
			double result = compareNumber(n1, n2);

			if (result > -1e-6 && result < 1e-6) {
				return 0;
			} else if (result > 0) {
				return 1;
			} else {
				return -1;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	protected <V> V eval(RowContext ctx, Expression child) {
		Evaluator<Expression, V> evaluator = lookup(Evaluator.class, child.getClass().getName());

		return (V) evaluator.evaluate(ctx, child);
	}

	@Override
	public Object getAggregatedValue() {
		throw new UnsupportedOperationException("Not an aggregator!");
	}

	@Override
	public boolean isAggregator() {
		return false;
	}
}
