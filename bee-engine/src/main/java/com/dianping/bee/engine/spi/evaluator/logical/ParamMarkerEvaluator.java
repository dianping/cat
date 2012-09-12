package com.dianping.bee.engine.spi.evaluator.logical;

import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.dianping.bee.engine.spi.RowContext;
import com.dianping.bee.engine.spi.evaluator.AbstractEvaluator;

public class ParamMarkerEvaluator extends AbstractEvaluator<ParamMarker, Object> {
	public static final String ID = ParamMarker.class.getName();

	@Override
	public Object evaluate(RowContext ctx, ParamMarker expr) {
		return ctx.getParameter(expr.getParamIndex() - 1);
	}
}
