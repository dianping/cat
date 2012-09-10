package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.dianping.bee.engine.spi.ColumnMeta;

public class SingleTablePreparedStatementBuilder extends SingleTableStatementBuilder {
	private List<ColumnMeta> m_paramColumns = new ArrayList<ColumnMeta>();

	@Override
	public void visit(DMLSelectStatement node) {
		super.visit(node);

		SingleTablePreparedStatement stmt = (SingleTablePreparedStatement) super.getStatement();
		Expression where = node.getWhere();

		if (where != null) {
			stmt.setParameterMetas(m_paramColumns);
		} else {
			stmt.setParameterMetas(Collections.<ColumnMeta> emptyList());
		}
	}

	@Override
	public void visit(ComparisionEqualsExpression node) {
		Expression left = node.getLeftOprand();

		if (left instanceof Identifier) {
			Expression right = node.getRightOprand();

			if (right instanceof ParamMarker) {
				String columnName = ((Identifier) left).getIdText();
				ColumnMeta column = findColumnBy(columnName);

				m_paramColumns.add(column);
			}
		}

		super.visit(node);
	}
}
