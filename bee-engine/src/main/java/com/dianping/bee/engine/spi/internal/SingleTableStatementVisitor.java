package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.site.lookup.annotation.Inject;

public class SingleTableStatementVisitor extends EmptySQLASTVisitor {
	@Inject
	private TableHelper m_helper;

	@Inject
	private Statement m_stmt;

	@Inject
	private RowFilter m_rowFilter;

	private String m_alias;

	private String m_tableName;

	private Clause m_clause;

	private List<ColumnMeta> m_selectColumns = new ArrayList<ColumnMeta>();

	private List<ColumnMeta> m_whereColumns = new ArrayList<ColumnMeta>();

	public Statement getStatement() {
		return m_stmt;
	}

	@Override
	public void visit(DMLSelectStatement node) {
		TableReference tr = node.getTables();

		m_clause = Clause.TABLE;

		if (tr.isSingleTable()) {
			tr.accept(this);
		} else {
			throw new RuntimeException("Not a single table query!");
		}

		List<Pair<Expression, String>> exprList = node.getSelectExprList();

		m_clause = Clause.SELECT;

		for (Pair<Expression, String> pair : exprList) {
			String alias = pair.getValue();

			if (alias != null && !alias.equals(m_alias)) {
				throw new BadSQLSyntaxException("Invalid select alias(%s)!", alias);
			}

			pair.getKey().accept(this);
		}

		m_stmt.setSelectColumns(m_selectColumns);

		m_clause = Clause.WHERE;

		Expression where = node.getWhere();

		if (where != null) {
			// to get columns from where clause
			where.accept(this);

			// to evaluate where clause
			m_rowFilter.setExpression(where);
			m_stmt.setRowFilter(m_rowFilter);
		}
	}

	@Override
	public void visit(Identifier node) {
		switch (m_clause) {
		case SELECT:
			String selectColumnName = node.getIdTextUpUnescape();

			findOrCreateColumnFrom(m_selectColumns, selectColumnName);
			break;
		case WHERE:
			String whereColumnName = node.getIdTextUpUnescape();

			findOrCreateColumnFrom(m_whereColumns, whereColumnName);
			break;
		}
	}

	private ColumnMeta findOrCreateColumnFrom(List<ColumnMeta> columns, String columnName) {
		for (ColumnMeta column : columns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}

		ColumnMeta column = m_helper.findColumn(m_tableName, columnName);

		columns.add(column);
		return column;
	}

	@Override
	public void visit(TableRefFactor node) {
		m_alias = node.getAlias();
		m_tableName = node.getTable().getIdTextUpUnescape();
	}

	static enum Clause {
		SELECT,

		TABLE,

		WHERE,

		GROUP,

		HAVING,

		ORDER;
	}
}
