package com.dianping.bee.engine.spi.internal;

import com.alibaba.cobar.parser.ast.fragment.tableref.TableReferences;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;

public class QueryDetector extends EmptySQLASTVisitor {
	private boolean m_singleTable;

	public boolean isSingleTable() {
		return m_singleTable;
	}

	@Override
	public void visit(DMLSelectStatement node) {
		TableReferences tables = node.getTables();

		m_singleTable = tables.isSingleTable();
	}
}
