package com.dianping.bee.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Index;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.RowSet;
import com.dianping.bee.engine.spi.meta.internal.DefaultCell;
import com.dianping.bee.engine.spi.meta.internal.DefaultRow;
import com.dianping.bee.engine.spi.meta.internal.DefaultRowSet;

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
			return null;
		}

		@Override
		public Index[] getIndexes() {
			return null;
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public RowSet queryByIndex(Index index, List<ColumnMeta> selectColumns) {
			List<ColumnMeta> columns = new ArrayList<ColumnMeta>();
			columns.addAll(selectColumns);
			DefaultRowSet rowSet = new DefaultRowSet(columns);
			for (int i = 0; i < 10; i++) {
				Cell[] cells = new Cell[columns.size()];
				for(int j=0;j<cells.length;j++){
					cells[j] = new DefaultCell(CatDatabase.TransactionColumn.valueOf(columns.get(j).getName()),RandomStringUtils.randomAlphanumeric(5));
				}
				Row row = new DefaultRow(cells);
				rowSet.addRow(row);
			}
			return rowSet;
		}
	}

	public static enum TransactionColumn implements ColumnMeta {
		Id(String.class), // hash, auto generated

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

	public static enum TransactionIndex implements Index {
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
	}
}
