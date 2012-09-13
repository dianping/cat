package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.ast.expression.comparison.ComparisionEqualsExpression;
import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.expression.primary.Wildcard;
import com.alibaba.cobar.parser.ast.expression.primary.function.FunctionExpression;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralNumber;
import com.alibaba.cobar.parser.ast.expression.primary.literal.LiteralString;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableReference;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLSelectStatement;
import com.alibaba.cobar.parser.util.Pair;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;
import com.dianping.bee.engine.evaluator.Evaluator;
import com.dianping.bee.engine.helper.SqlParsers;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.TableProvider;
import com.site.lookup.annotation.Inject;

public class SingleTableStatementBuilder extends EmptySQLASTVisitor implements Contextualizable {
	@Inject
	private SingleTableStatement m_stmt;

	@Inject
	private SingleTableRowFilter m_rowFilter;

	@Inject
	private TableHelper m_helper;

	private PlexusContainer m_container;

	private String m_alias;

	private String m_tableName;

	private String m_databaseName;

	private Clause m_clause;

	private List<ColumnMeta> m_columns = new ArrayList<ColumnMeta>();

	private List<ColumnMeta> m_whereColumns = new ArrayList<ColumnMeta>();

	public void contextualize(Context context) throws ContextException {
		m_container = (PlexusContainer) context.get("plexus");
	}

	protected ColumnMeta findColumnBy(String columnName) {
		return m_helper.findColumn(m_databaseName, m_tableName, columnName);
	}

	protected ColumnMeta findOrCreateColumnFrom(List<ColumnMeta> columns, String columnName) {
		for (ColumnMeta column : columns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}

		ColumnMeta column = findColumnBy(columnName);

		columns.add(column);
		return column;
	}

	protected SingleTableStatement getStatement() {
		return m_stmt;
	}

	@SuppressWarnings("unchecked")
	protected Class<?> getTypeForExpression(Expression expr) {
		try {
			Evaluator<Expression, Object> evaluator = m_container.lookup(Evaluator.class, expr.getClass().getName());

			return evaluator.getResultType(expr);
		} catch (ComponentLookupException e) {
			throw new RuntimeException(String.format("No evaluator defined for %s.", expr.getClass().getName()), e);
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
		List<SelectField> fields = new ArrayList<SelectField>();

		for (Pair<Expression, String> item : exprList) {
			String alias = item.getValue();

			if (alias != null && !alias.equals(m_alias)) {
				throw new BadSQLSyntaxException("Invalid select alias(%s), expected: null or %s!", alias, m_alias);
			}

			Expression expr = item.getKey();

			if (expr instanceof Wildcard) { // "*", expand it
				TableProvider table = m_helper.findTable(m_databaseName, m_tableName);

				for (ColumnMeta column : table.getColumns()) {
					fields.add(new SelectField(column, null));
				}
			} else if (expr instanceof Identifier) { // column
				String columnName = ((Identifier) expr).getIdText();
				ColumnMeta column = findColumnBy(SqlParsers.forEscape().unescape(columnName));

				fields.add(new SelectField(column, alias));
			} else { // expression
				fields.add(new SelectField(expr, alias, getTypeForExpression(expr)));
			}

			expr.accept(this);
		}

		m_stmt.setSelectFields(fields);

		// for where clause
		m_clause = Clause.WHERE;

		Expression where = node.getWhere();

		if (where != null) {
			// to get columns from where clause
			where.accept(this);

			// to evaluate where clause
			m_stmt.setRowFilter(m_rowFilter.setExpression(where));
			m_stmt.setColumns(m_columns);
			m_stmt.setIndex(m_helper.findIndex(m_databaseName, m_tableName, m_whereColumns));
		} else {
			m_stmt.setColumns(m_columns);
			m_stmt.setIndex(m_helper.findDefaultIndex(m_databaseName, m_tableName));
		}

		if (m_stmt.getIndexMeta() == null) {
			throw new BadSQLSyntaxException("Invalid index column!");
		}
	}

	@Override
	public void visit(Identifier node) {
		switch (m_clause) {
		case SELECT:
			String selectColumnName = node.getIdTextUpUnescape();

			if (node instanceof Wildcard) { // expand it
				TableProvider table = m_helper.findTable(m_databaseName, m_tableName);
				ColumnMeta[] columnMetas = table.getColumns();

				for (ColumnMeta meta : columnMetas) {
					m_columns.add(meta);
				}
			} else {
				findOrCreateColumnFrom(m_columns, selectColumnName);
			}

			break;
		case WHERE:
			String whereColumnName = node.getIdTextUpUnescape();

			findOrCreateColumnFrom(m_columns, whereColumnName);
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
