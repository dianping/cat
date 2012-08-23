package com.dianping.bee.engine.spi;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.dianping.bee.engine.spi.meta.Row;

public interface RowFilter {
	public boolean filter(Row row);

	public void setExpression(Expression where);
}
