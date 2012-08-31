package com.dianping.bee.db;

import org.apache.commons.lang3.RandomStringUtils;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.index.Index;
import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.IndexMeta;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.meta.internal.DefaultCell;
import com.dianping.bee.engine.spi.meta.internal.DefaultRow;
import com.dianping.bee.engine.spi.meta.internal.DefaultRowSet;

public class CatDatabase implements DatabaseProvider {

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

		Event("event") {
			@Override
			public EventColumn[] getColumns() {
				return EventColumn.values();
			}

			@Override
			public EventIndex[] getIndexes() {
				return EventIndex.values();
			}
		},

		Heartbeat("heartbeat"),

		Problem("problem");

		private String m_name;

		private CatTable(String name) {
			m_name = name;
		}

		@Override
		public ColumnMeta[] getColumns() {
			return null;
		}

		@Override
		public IndexMeta[] getIndexes() {
			return null;
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public RowSet queryByIndex(IndexMeta index, ColumnMeta[] selectColumns) {
			ColumnMeta[] columns = selectColumns;
			DefaultRowSet rowSet = new DefaultRowSet(columns);

			for (int rowIndex = 0; rowIndex < 10; rowIndex++) {
				Cell[] cells = new Cell[columns.length];
				for (int colIndex = 0; colIndex < cells.length; colIndex++) {
					ColumnMeta columnMeta = columns[colIndex];
					String randomValue = null;
					if (columnMeta.getType().getSimpleName().equals("String")) {
						randomValue = RandomStringUtils.randomAlphabetic(5);
					} else if (columnMeta.getType().getSimpleName().equals("Integer")
					      || columnMeta.getType().getSimpleName().equals("Long")) {
						randomValue = RandomStringUtils.randomNumeric(3);
					} else {
						randomValue = RandomStringUtils.randomAlphanumeric(5);
					}
					cells[colIndex] = new DefaultCell(columnMeta, randomValue);
				}

				Row row = new DefaultRow(cells);
				rowSet.addRow(row);
			}
			return rowSet;
		}
	}

	public static enum EventColumn implements ColumnMeta {
		StartTime(String.class), // 20120822(for daily), 2012082213(for hour)

		Domain(String.class), // MobileApi

		Type(String.class), // URL

		Name(String.class), // /deallist.bin

		TotalCount(Integer.class), // 2033

		Failures(Integer.class), // 5

		SampleMessage(String.class), // MobileApi-0a0101a6-1345600834200-1

		MinDuration(Integer.class), // 1

		MaxDuration(Integer.class), // 1234

		SumDuration(Long.class), // 123456

		Sum2Duration(Long.class), // 2364233

		Line95(Integer.class); // 123

		public static EventColumn findByName(String name) {
			for (EventColumn column : values()) {
				if (column.getName().equalsIgnoreCase(name)) {
					return column;
				}
			}

			throw new RuntimeException(String.format("Column(%s) is not found in %s", name, EventColumn.class.getName()));
		}

		private String m_name;

		private Class<?> m_type;

		private EventColumn(Class<?> type) {
			m_type = type;
			m_name = name().toLowerCase();
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public Class<?> getType() {
			return m_type;
		}
	}

	public static enum EventIndex implements IndexMeta {
		IDX_STARTTIME_DOMAIN(EventColumn.StartTime, false, EventColumn.Domain, true);

		private ColumnMeta[] m_columns;

		private boolean[] m_orders;

		private EventIndex(Object... args) {
			int length = args.length;

			if (length % 2 != 0) {
				throw new IllegalArgumentException(String.format("Parameters should be paired for %s(%s)!", getClass(),
				      name()));
			}

			m_columns = new ColumnMeta[length / 2];
			m_orders = new boolean[length / 2];

			for (int i = 0; i < length / 2; i++) {
				m_columns[i] = (ColumnMeta) args[2 * i];
				m_orders[i] = (Boolean) args[2 * i + 1];
			}
		}

		@Override
		public ColumnMeta getColumn(int index) {
			if (index >= 0 && index < m_columns.length) {
				return m_columns[index];
			} else {
				throw new IndexOutOfBoundsException("size: " + m_columns.length + ", index: " + index);
			}
		}

		@Override
		public int getLength() {
			return m_columns.length;
		}

		@Override
		public boolean isAscend(int index) {
			if (index >= 0 && index < m_orders.length) {
				return m_orders[index];
			} else {
				throw new IndexOutOfBoundsException("size: " + m_orders.length + ", index: " + index);
			}
		}

		@Override
      public Class<? extends Index<?>> getIndexClass() {
			throw new UnsupportedOperationException("Not implemented yet!");
      }
	}

	public static enum TransactionColumn implements ColumnMeta {
		StartTime(String.class), // 20120822(for daily), 2012082213(for hour)

		Domain(String.class), // MobileApi

		Type(String.class), // URL

		Name(String.class), // /deallist.bin

		TotalCount(Integer.class), // 2033

		Failures(Integer.class), // 5

		SampleMessage(String.class), // MobileApi-0a0101a6-1345600834200-1

		MinDuration(Integer.class), // 1

		MaxDuration(Integer.class), // 1234

		SumDuration(Long.class), // 123456

		Sum2Duration(Long.class), // 2364233

		Line95(Integer.class); // 123

		private String m_name;

		private Class<?> m_type;

		private TransactionColumn(Class<?> type) {
			m_type = type;
			m_name = name().toLowerCase();
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public Class<?> getType() {
			return m_type;
		}
	}

	public static enum TransactionIndex implements IndexMeta {
		IDX_STARTTIME_DOMAIN(TransactionColumn.StartTime, false, TransactionColumn.Domain, true);

		private ColumnMeta[] m_columns;

		private boolean[] m_orders;

		private TransactionIndex(Object... args) {
			int length = args.length;

			if (length % 2 != 0) {
				throw new IllegalArgumentException(String.format("Parameters should be paired for %s(%s)!", getClass(),
				      name()));
			}

			m_columns = new ColumnMeta[length / 2];
			m_orders = new boolean[length / 2];

			for (int i = 0; i < length / 2; i++) {
				m_columns[i] = (ColumnMeta) args[2 * i];
				m_orders[i] = (Boolean) args[2 * i + 1];
			}
		}

		@Override
		public ColumnMeta getColumn(int index) {
			if (index >= 0 && index < m_columns.length) {
				return m_columns[index];
			} else {
				throw new IndexOutOfBoundsException("size: " + m_columns.length + ", index: " + index);
			}
		}

		@Override
		public int getLength() {
			return m_columns.length;
		}

		@Override
		public boolean isAscend(int index) {
			if (index >= 0 && index < m_orders.length) {
				return m_orders[index];
			} else {
				throw new IndexOutOfBoundsException("size: " + m_orders.length + ", index: " + index);
			}
		}

		@Override
      public Class<? extends Index<?>> getIndexClass() {
			throw new UnsupportedOperationException("Not implemented yet!");
      }
	}

	@Override
	public String getName() {
		return "cat";
	}

	@Override
	public CatTable[] getTables() {
		return CatTable.values();
	}
}
