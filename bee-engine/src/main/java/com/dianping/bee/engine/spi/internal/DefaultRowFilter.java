package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.dianping.bee.engine.spi.RowFilter;

public class DefaultRowFilter implements RowFilter {
	private Expression m_expr;

	@Override
   public boolean filter(List<Object> values) {
	   return false;
   }

	@Override
   public void setExpression(Expression expr) {
		m_expr = expr;
   }
}
