package com.dianping.bee.db;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.RowSet;
import com.dianping.bee.engine.spi.Statement;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.ColumnMeta;

public class CatDatabase implements DatabaseProvider {
	@Override
	public String getName() {
		return "cat";
	}

	@Override
	public CatTable[] getTables() {
		return CatTable.values();
	}

	public static enum CatTable implements TableProvider {
		Transaction("transaction") {
			@Override
			public TransactionColumn[] getColumns() {
				return TransactionColumn.values();
			}

			@Override
			public TransactionIndex[] getIndexes() {
				return TransactionIndex.values();
			}
		},

		Event("event"),

		Heartbeat("heartbeat"),

		Problem("problem");

		private String m_name;

		private CatTable(String name) {
			m_name = name;
		}

		@Override
		public ColumnMeta[] getColumns() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Index[] getIndexes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public RowSet query(Statement stmt) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public static enum TransactionColumn implements ColumnMeta {
		Type(String.class),

		Name(String.class),

		TotalCount(Integer.class),

		Failures(Integer.class),

		SampleLink(String.class),

		MinDuration(Integer.class),

		MaxDuration(Integer.class),

		SumDuration(Long.class),

		Sum2Duration(Long.class),

		Line95(Integer.class);

		private String m_name;

		private Class<?> m_type;

		private TransactionColumn(Class<?> type) {
			m_type = type;
			m_name = name().toLowerCase();
		}

		@Override
		public Object getName() {
			return m_name;
		}
	}

	public static enum TransactionIndex implements Index {
		IDX_TYPE_NAME {
			@Override
			public ColumnMeta getColumn(int index) {
				switch (index) {
				case 0:
					return TransactionColumn.Type;
				case 1:
					return TransactionColumn.Name;
				}

				throw new RuntimeException("Internal error happened!");
			}

			@Override
			public int getLength() {
				return 2;
			}

			@Override
			public boolean isAscend(int index) {
				return true;
			}
		};
	}
}
