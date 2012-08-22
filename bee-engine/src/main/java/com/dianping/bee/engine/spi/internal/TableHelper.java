package com.dianping.bee.engine.spi.internal;

import java.util.List;

import com.dianping.bee.engine.spi.Index;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.site.lookup.annotation.Inject;

public class TableHelper {
	@Inject
	private TableProviderManager m_manager;

	public ColumnMeta findColumn(String tableName, String columnName) {
		TableProvider table = findTable(tableName);
		ColumnMeta[] columns = table.getColumns();

		for (ColumnMeta column : columns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}

		throw new BadSQLSyntaxException("Column(%s) of table(%s) is not found!", columnName, tableName);
	}

	public Index findIndex(String tableName, List<ColumnMeta> columns) {
		TableProvider table = findTable(tableName);
		Index[] indexes = table.getIndexes();

		if (indexes != null && indexes.length > 0) {
			for (Index index : indexes) {
				// if first column of index is in columns, then pick it up
				ColumnMeta first = index.getColumn(0);
				String columnName = first.getName();

				for (ColumnMeta column : columns) {
					if (column.getName().equals(columnName)) {
						return index;
					}
				}
			}
		}

		return null;
	}

	public TableProvider findTable(String tableName) {
		TableProvider table = m_manager.getTableProvider(tableName);

		if (table == null) {
			throw new BadSQLSyntaxException("Table(%s) is not found!", tableName);
		} else {
			return table;
		}
	}
}
