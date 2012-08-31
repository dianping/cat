package com.dianping.bee.engine.spi.meta;

import com.dianping.bee.engine.spi.row.RowContext;

public interface Index {
	public IndexMeta getMeta();

	public void setValue(int index, Object value);

	public void addValue(int index, Object value, PredicateType type);

	public void findByKey(RowContext ctx);
	
	public void findByRange(RowContext ctx);

	public static enum PredicateType {
		LT("<"),

		LE("<="),

		EQ("="),

		GT(">"),

		GE(">=");

		private String m_symbol;

		private PredicateType(String symbol) {
			m_symbol = symbol;
		}

		public String getSymbol() {
			return m_symbol;
		}
	}
}
