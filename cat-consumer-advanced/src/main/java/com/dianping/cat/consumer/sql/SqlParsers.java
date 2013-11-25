package com.dianping.cat.consumer.sql;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cobar.parser.ast.expression.primary.Identifier;
import com.alibaba.cobar.parser.ast.fragment.tableref.TableRefFactor;
import com.alibaba.cobar.parser.ast.stmt.SQLStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLDeleteStatement;
import com.alibaba.cobar.parser.ast.stmt.dml.DMLInsertStatement;
import com.alibaba.cobar.parser.recognizer.SQLParserDelegate;
import com.alibaba.cobar.parser.visitor.EmptySQLASTVisitor;

public class SqlParsers {
	public static TableParser forTable() {
		return new TableParser();
	}

	public static class TableParser extends EmptySQLASTVisitor {
		private List<String> m_tables = new ArrayList<String>(3);

		public List<String> parse(String sql) {
			try {
				SQLStatement statement = SQLParserDelegate.parse(sql);

				statement.accept(this);
				return m_tables;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void visit(DMLDeleteStatement node) {
			for (Identifier tableIdentifier : node.getTableNames()) {
				String table = tableIdentifier.getIdText();

				if (!m_tables.contains(table)) {
					m_tables.add(table);
				}
			}

			super.visit(node);
		}

		@Override
		public void visit(DMLInsertStatement node) {
			Identifier tableIdentifier = node.getTable();
			String table = tableIdentifier.getIdText();

			if (!m_tables.contains(table)) {
				m_tables.add(table);
			}

			super.visit(node);
		}

		@Override
		public void visit(TableRefFactor node) {
			String table = node.getTable().getIdText();

			if (!m_tables.contains(table)) {
				m_tables.add(table);
			}
		}
	}
}
