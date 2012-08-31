package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.ParamMarker;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;
import com.dianping.bee.engine.spi.RowFilter;
import com.dianping.bee.engine.spi.SingleTableStatement;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.site.lookup.annotation.Inject;

public class SingleTableStatementVisitor extends EmptySQLASTVisitor {
	static enum Clause {
		SELECT,

		TABLE,

		WHERE,

		GROUP,

		HAVING,

		ORDER;
	}

	@Inject
	private TableHelper m_helper;

	@Inject
	private SingleTableStatement m_stmt;

	@Inject
	private RowFilter m_rowFilter;

	private String m_alias;

	private String m_tableName;

	private String m_databaseName;

	private int m_parameterSize;

	private Clause m_clause;

	private List<ColumnMeta> m_selectColumns = new ArrayList<ColumnMeta>();

	private List<ColumnMeta> m_whereColumns = new ArrayList<ColumnMeta>();

	private boolean checkSelectAll(List<ColumnMeta> columns, String columnName) {
		if ("*".equals(columnName)) {
			TableProvider table = m_helper.findTable(m_tableName);
			if (table != null) {
				ColumnMeta[] columnMetas = table.getColumns();
				if (columnMetas != null) {
					for (ColumnMeta meta : columnMetas) {
						columns.add(meta);
					}
				}
			}
			return true;
		}
		return false;
	}

	private ColumnMeta findOrCreateColumnFrom(List<ColumnMeta> columns, String columnName) {
		for (ColumnMeta column : columns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}

		ColumnMeta column = m_helper.findColumn(m_databaseName, m_tableName, columnName);

		columns.add(column);
		return column;
	}

	public SingleTableStatement getStatement() {
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

		if (m_databaseName == null) {
			m_stmt.setTable(m_helper.findTable(m_tableName));
		} else {
			m_stmt.setTable(m_helper.findTable(m_databaseName, m_tableName));
		}

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

		ColumnMeta[] columnMetas = new ColumnMeta[m_selectColumns.size()];
		m_stmt.setSelectColumns(m_selectColumns.toArray(columnMetas));

		// for where clause
		m_clause = Clause.WHERE;

		Expression where = node.getWhere();

		if (where != null) {
			// to get columns from where clause
			where.accept(this);

			// to evaluate where clause
			m_rowFilter.setExpression(where);
			m_stmt.setRowFilter(m_rowFilter);
			if (m_databaseName == null) {
				m_stmt.setIndex(m_helper.findIndex(m_tableName, m_whereColumns));
			} else {
				m_stmt.setIndex(m_helper.findIndex(m_databaseName, m_tableName, m_whereColumns));
			}

			m_stmt.setParameterSize(m_parameterSize);
		}
	}

	@Override
	public void visit(Identifier node) {
		switch (m_clause) {
		case SELECT:
			String selectColumnName = node.getIdTextUpUnescape();

			if (!checkSelectAll(m_selectColumns, selectColumnName)) {
				findOrCreateColumnFrom(m_selectColumns, selectColumnName);
			}
			break;
		case WHERE:
			String whereColumnName = node.getIdTextUpUnescape();

			findOrCreateColumnFrom(m_whereColumns, whereColumnName);
			break;
		case TABLE:
			break;
		case GROUP:
			break;
		case HAVING:
			break;
		case ORDER:
			break;
		default:
			;
		}
	}

	@Override
	public void visit(TableRefFactor node) {
		m_alias = node.getAlias();
		m_tableName = node.getTable().getIdTextUpUnescape();
		if (node.getTable().getParent() != null) {
			m_databaseName = node.getTable().getParent().getIdTextUpUnescape();
		}
	}

	@Override
	public void visit(ParamMarker node) {
		m_parameterSize++;
	}
}
