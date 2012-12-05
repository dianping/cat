package com.dianping.bee.engine.spi.internal;

import com.alibaba.cobar.parser.ast.expression.Expression;
import com.alibaba.cobar.parser.visitor.MySQLOutputASTVisitor;
import com.dianping.bee.engine.evaluator.Evaluator;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.RowContext;

class SelectField implements ColumnMeta {
	private String m_name;

	private Class<?> m_type;

	private ColumnMeta m_column;

	private Expression m_expr;

	private Evaluator<Expression, Object> m_evaluator;

	public SelectField(ColumnMeta column, String name) {
		m_column = column;
		m_name = name != null ? name : column.getName();
		m_type = column.getType();
	}

	public SelectField(Expression expr, String alias, Class<?> type) {
		m_expr = expr;
		m_type = type;

		if (alias != null) {
			m_name = alias;
		} else {
			m_name = asString(expr);
		}
	}

	private String asString(Expression expr) {
		StringBuilder sb = new StringBuilder();
		MySQLOutputASTVisitor visitor = new MySQLOutputASTVisitor(sb);

		expr.accept(visitor);
		return sb.toString();
	}

	public Object evaluate(RowContext ctx, int colIndex) {
		if (m_column != null) {
			return ctx.getValue(colIndex);
		} else if (m_expr != null) {
			Evaluator<Expression, Object> evaluator = getEvaluator(ctx);
			Object value = evaluator.evaluate(ctx, m_expr);

			return value;
		} else {
			throw new RuntimeException("Internal error: should not reach here!");
		}
	}

	public Object getAggregatedValue() {
		return getEvaluator(null).getAggregatedValue();
	}

	@SuppressWarnings("unchecked")
	private Evaluator<Expression, Object> getEvaluator(RowContext ctx) {
		if (m_evaluator == null) {
			String name = m_expr.getClass().getName();
			Evaluator<Expression, Object> evaluator = (Evaluator<Expression, Object>) ctx.lookupComponent(Evaluator.class, name);

			m_evaluator = evaluator;
		}

		return m_evaluator;
	}

	public String getName() {
		return m_name;
	}

	public Class<?> getType() {
		return m_type;
	}

	@SuppressWarnings("unchecked")
	public boolean isAggregator(RowContext ctx) {
		if (m_expr != null) {
			String name = m_expr.getClass().getName();
			Evaluator<Expression, Object> evaluator = (Evaluator<Expression, Object>) ctx.lookupComponent(Evaluator.class, name);

			return evaluator.isAggregator();
		} else {
			return false;
		}
	}

	public void reset(RowContext ctx) {
		if (m_evaluator != null) {
			ctx.releaseComponent(m_evaluator);
			m_evaluator = null;
		}
	}
}