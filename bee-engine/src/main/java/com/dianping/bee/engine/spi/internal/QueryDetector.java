package com.dianping.bee.engine.spi.internal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;

public class QueryDetector extends EmptySQLASTVisitor {
	private boolean m_singleTable;

	private boolean m_isPrepared;

	public boolean isPrepared() {
		return m_isPrepared;
	}

	public boolean isSingleTable() {
		return m_singleTable;
	}

	@Override
	public void visit(DMLSelectStatement node) {
		TableReferences tables = node.getTables();

		m_singleTable = tables.isSingleTable();
		
		Expression where = node.getWhere();

		if (where != null) {
			where.accept(this);
		}
	}

	@Override
	public void visit(ComparisionEqualsExpression node) {
		Expression left = node.getLeftOprand();

		if (left instanceof Identifier) {
			Expression right = node.getRightOprand();

			if (right instanceof ParamMarker) {
				m_isPrepared = true;
			}
		}

		super.visit(node);
	}
}
