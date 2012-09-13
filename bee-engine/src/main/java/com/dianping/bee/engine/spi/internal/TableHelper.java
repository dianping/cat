package com.dianping.bee.engine.spi.internal;

import java.util.ArrayList;
import java.util.List;

import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.IndexMeta;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.site.lookup.annotation.Inject;

public class TableHelper {
	@Inject
	private TableProviderManager m_manager;

	public ColumnMeta findColumn(String databaseName, String tableName, String columnName) {
		TableProvider table = findTable(databaseName, tableName);
		ColumnMeta[] columns = table.getColumns();

		if (columns != null) {
			for (ColumnMeta column : columns) {
				if (column.getName().equalsIgnoreCase(columnName)) {
					return column;
				}
			}
		}

		throw new BadSQLSyntaxException("Column(%s) of table(%s) is not found!", columnName, tableName);
	}

	public IndexMeta findIndex(String databaseName, String tableName, List<ColumnMeta> columns) {
		TableProvider table = findTable(databaseName, tableName);
		IndexMeta[] indexes = table.getIndexes();

		if (indexes != null && indexes.length > 0) {
			for (IndexMeta index : indexes) {
				// if first column of index is in columns, then pick it up
				// ColumnMeta first = index.getColumn(0);
				// String columnName = first.getName();
				// TODO how to determine index
				List<String> indexColumnNames = new ArrayList<String>();
				for (int i = 0; i < index.getLength(); i++) {
					indexColumnNames.add(index.getColumn(i).getName().toLowerCase());
				}
				for (ColumnMeta column : columns) {
					if (indexColumnNames.contains(column.getName().toLowerCase())) {
						return index;
					}
					// if (column.getName().equalsIgnoreCase(columnName)) {
					// return index;
					// }
				}
			}
		}

		return null;
	}

	public TableProvider findTable(String databaseName, String tableName) {
		TableProvider table = m_manager.getTableProvider(databaseName, tableName);

		if (table == null) {
			throw new BadSQLSyntaxException("Table(%s) is not found!", tableName);
		} else {
			return table;
		}
	}

	public IndexMeta findDefaultIndex(String databaseName, String tableName) {
		return findTable(databaseName, tableName).getDefaultIndex();
	}
}
