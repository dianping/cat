package com.dianping.bee.engine.spi;

import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;

public interface RowFilter {
	public boolean filter(List<Object> values);

	public void setExpression(Expression where);
}
