package com.dianping.bee.engine.spi.internal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;

public class SingleTablePreparedStatementBuilder extends SingleTableStatementBuilder {

	private int m_parameterSize;

	public SingleTablePreparedStatement getStatement() {
		return (SingleTablePreparedStatement) super.getStatement();
	}

	@Override
	public void visit(DMLSelectStatement node) {
		super.visit(node);
		Expression where = node.getWhere();
		if (where != null) {
			getStatement().setParameterSize(m_parameterSize);
		}
	}

	@Override
	public void visit(ParamMarker node) {
		m_parameterSize++;
	}
}
