package com.dianping.bee.engine.evaluator;

import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.dianping.bee.engine.spi.RowContext;

public class ParamMarkerEvaluator extends AbstractEvaluator<ParamMarker, Object> {
	public static final String ID = ParamMarker.class.getName();

	@Override
	public Object evaluate(RowContext ctx, ParamMarker expr) {
		return ctx.getParameter(expr.getParamIndex() - 1);
	}

	@Override
	public Class<?> getResultType(ParamMarker expr) {
		throw new UnsupportedOperationException("This method should not be called!");
	}
}
