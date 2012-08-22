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

	public Statement getStatement() {
		return m_stmt;
	}

	@Override
	public void visit(DMLSelectStatement node) {
		// for from clause
		m_clause = Clause.TABLE;

		TableReference tr = node.getTables();

		if (tr.isSingleTable()) {
			tr.accept(this);
		} else {
			throw new RuntimeException("Not a single table query!");
		}

		m_stmt.setTableName(m_tableName);

		// for select clause
		m_clause = Clause.SELECT;

		List<Pair<Expression, String>> exprList = node.getSelectExprList();

		for (Pair<Expression, String> pair : exprList) {
			String alias = pair.getValue();

			if (alias != null && !alias.equals(m_alias)) {
				throw new BadSQLSyntaxException("Invalid select alias(%s)!", alias);
			}

			pair.getKey().accept(this);
		}

		m_stmt.setSelectColumns(m_selectColumns);

		// for where clause
		m_clause = Clause.WHERE;

		Expression where = node.getWhere();

		if (where != null) {
			// to get columns from where clause
			where.accept(this);

			// to evaluate where clause
			m_rowFilter.setExpression(where);
			m_stmt.setRowFilter(m_rowFilter);
			m_stmt.setIndex(m_helper.findIndex(m_tableName, m_whereColumns));
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
