package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.site.lookup.annotation.Inject;

public class SingleTableStatementBuilder extends EmptySQLASTVisitor {
	@Inject
	private SingleTableStatement m_stmt;

	@Inject
	private SingleTableRowFilter m_rowFilter;

	@Inject
	private TableHelper m_helper;

	private String m_alias;

	private String m_tableName;

	private String m_databaseName;

	private Clause m_clause;

	private List<ColumnMeta> m_selectColumns = new ArrayList<ColumnMeta>();

	private List<ColumnMeta> m_whereColumns = new ArrayList<ColumnMeta>();

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

		// for select clause
		m_clause = Clause.SELECT;

		List<Pair<Expression, String>> exprList = node.getSelectExprList();

		for (Pair<Expression, String> expr : exprList) {
			String alias = expr.getValue();

			if (alias != null && !alias.equals(m_alias)) {
				throw new BadSQLSyntaxException("Invalid select alias(%s), expected: null or %s!", alias, m_alias);
			}

			expr.getKey().accept(this);
		}

		m_stmt.setSelectColumns(m_selectColumns);

		// for where clause
		m_clause = Clause.WHERE;

		Expression where = node.getWhere();

		if (where != null) {
			// to get columns from where clause
			where.accept(this);

			// to evaluate where clause
			m_stmt.setWhereColumns(m_whereColumns);
			m_stmt.setRowFilter(m_rowFilter.setExpression(where));
			m_stmt.setIndex(m_helper.findIndex(m_databaseName, m_tableName, m_whereColumns));
		} else {
			m_stmt.setIndex(m_helper.findDefaultIndex(m_databaseName, m_tableName));
		}
	}

	@Override
	public void visit(Identifier node) {
		switch (m_clause) {
		case SELECT:
			String selectColumnName = node.getIdTextUpUnescape();

			if ("*".equals(selectColumnName)) { // expand it
				TableProvider table = m_helper.findTable(m_databaseName, m_tableName);
				ColumnMeta[] columnMetas = table.getColumns();

				for (ColumnMeta meta : columnMetas) {
					m_selectColumns.add(meta);
				}
			} else {
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
			break;
		}
	}

	@Override
	public void visit(ComparisionEqualsExpression node) {
		Expression left = node.getLeftOprand();

		if (left instanceof Identifier) {
			Expression right = node.getRightOprand();

			if (right instanceof LiteralString) {
				String name = ((Identifier) left).getIdText();
				String value = ((LiteralString) right).getString();

				m_stmt.addAttribute(name, value);
			} else if (right instanceof LiteralNumber) {
				String name = ((Identifier) left).getIdText();
				Number value = ((LiteralNumber) right).getNumber();

				m_stmt.addAttribute(name, value);
			}
		}

		super.visit(node);
	}

	@Override
	public void visit(TableRefFactor node) {
		m_alias = node.getAlias();
		m_tableName = node.getTable().getIdTextUpUnescape();

		if (node.getTable().getParent() != null) {
			m_databaseName = node.getTable().getParent().getIdTextUpUnescape();
		}
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
