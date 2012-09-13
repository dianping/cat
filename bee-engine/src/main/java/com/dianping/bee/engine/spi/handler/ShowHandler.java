package com.dianping.bee.engine.spi.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.helper.SqlParsers;
import com.dianping.bee.engine.helper.SqlWildcard;
import com.dianping.bee.engine.helper.TypeUtils;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.IndexMeta;
import com.dianping.bee.engine.spi.SessionManager;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.server.mysql.InformationSchemaDatabaseProvider;
import com.site.lookup.LookupException;
import com.site.lookup.annotation.Inject;

public class ShowHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(ShowHandler.class);

	@Inject
	private SessionManager m_sessionManager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
		int len = parts.size();
		String first = len > 0 ? parts.get(0) : null;
		String second = len > 1 ? parts.get(1) : null;
		String third = len > 2 ? parts.get(2) : null;
		String forth = len > 3 ? parts.get(3) : null;

		if ("databases".equalsIgnoreCase(first)) {
			showDatabases(c);
		} else if ("tables".equalsIgnoreCase(first)) {
			String databaseName = len > 2 && "from".equalsIgnoreCase(parts.get(1)) ? parts.get(2) : c.getSchema();
			String pattern = len > 2 && "like".equalsIgnoreCase(parts.get(1)) ? parts.get(2) : null;
			pattern = len > 4 && "like".equalsIgnoreCase(parts.get(3)) ? parts.get(4) : pattern;
			showTables(c, SqlParsers.forEscape().unescape(databaseName), SqlParsers.forEscape().unescape(pattern), false);
		} else if ("columns".equalsIgnoreCase(first) && len > 2) {
			String databaseName = len > 5 ? parts.get(4) : c.getSchema();
			String pattern = len > 4 && "like".equalsIgnoreCase(parts.get(3)) ? parts.get(4) : null;
			pattern = len > 6 && "like".equalsIgnoreCase(parts.get(5)) ? parts.get(6) : pattern;
			showColumns(c, SqlParsers.forEscape().unescape(databaseName), SqlParsers.forEscape().unescape(third), SqlParsers
			      .forEscape().unescape(pattern), false);
		} else if ("keys".equalsIgnoreCase(first) && "from".equalsIgnoreCase(second)) {
			String databaseName = len > 5 ? parts.get(4) : c.getSchema();
			String pattern = len > 4 && "like".equalsIgnoreCase(parts.get(3)) ? parts.get(4) : null;
			pattern = len > 6 && "like".equalsIgnoreCase(parts.get(5)) ? parts.get(6) : pattern;
			showIndexes(c, SqlParsers.forEscape().unescape(databaseName), SqlParsers.forEscape().unescape(third), SqlParsers
			      .forEscape().unescape(pattern));
		} else if ("index".equalsIgnoreCase(first)) {
			String databaseName = len > 5 ? parts.get(4) : c.getSchema();
			String pattern = len > 4 && "like".equalsIgnoreCase(parts.get(3)) ? parts.get(4) : null;
			pattern = len > 6 && "like".equalsIgnoreCase(parts.get(5)) ? parts.get(6) : pattern;
			showIndexes(c, SqlParsers.forEscape().unescape(databaseName), SqlParsers.forEscape().unescape(third), SqlParsers
			      .forEscape().unescape(pattern));
		} else if ("indexes".equalsIgnoreCase(first)) {
			String databaseName = len > 5 ? parts.get(4) : c.getSchema();
			String pattern = len > 4 && "like".equalsIgnoreCase(parts.get(3)) ? parts.get(4) : null;
			pattern = len > 6 && "like".equalsIgnoreCase(parts.get(5)) ? parts.get(6) : pattern;
			showIndexes(c, SqlParsers.forEscape().unescape(databaseName), SqlParsers.forEscape().unescape(third), SqlParsers
			      .forEscape().unescape(pattern));
		} else if ("table".equalsIgnoreCase(first) && "status".equalsIgnoreCase(second)) {
			String databaseName = len > 3 ? parts.get(3) : c.getSchema();
			String pattern = len > 3 && "like".equalsIgnoreCase(parts.get(2)) ? parts.get(3) : null;
			pattern = len > 5 && "like".equalsIgnoreCase(parts.get(4)) ? parts.get(5) : pattern;
			showTableStatus(c, SqlParsers.forEscape().unescape(databaseName), SqlParsers.forEscape().unescape(pattern));
		} else if ("status".equalsIgnoreCase(first)) {
			showStatus(c);
		} else if ("variables".equalsIgnoreCase(first)) {
			showVariables(c);
		} else if ("collation".equalsIgnoreCase(first)) {
			showCollation(c);
		} else if ("full".equalsIgnoreCase(first) && "tables".equalsIgnoreCase(second) && "from".equalsIgnoreCase(third)) {
			String pattern = len > 5 && "like".equalsIgnoreCase(parts.get(4)) ? parts.get(5) : null;
			showTables(c, SqlParsers.forEscape().unescape(forth), SqlParsers.forEscape().unescape(pattern), true);
		} else if ("full".equalsIgnoreCase(first) && "columns".equalsIgnoreCase(second) && "from".equalsIgnoreCase(third)) {
			String databaseName = len > 5 && "from".equalsIgnoreCase(parts.get(4)) ? parts.get(5) : c.getSchema();
			String pattern = len > 5 && "like".equalsIgnoreCase(parts.get(4)) ? parts.get(5) : null;
			pattern = len > 7 && "like".equalsIgnoreCase(parts.get(6)) ? parts.get(7) : pattern;
			showColumns(c, SqlParsers.forEscape().unescape(databaseName), SqlParsers.forEscape().unescape(forth), SqlParsers
			      .forEscape().unescape(pattern), true);
		} else {
			error(c, ErrorCode.ER_UNKNOWN_COM_ERROR, String.format("Unsupported show command(%s)", parts.toString()));
		}
	}

	private void showCollation(ServerConnection c) {
		LOGGER.info("showCollation");
		CommandContext ctx = new CommandContext(c);
		String[] names = { "Collation", "Charset", "Id", "Default", "Compiled", "Sortlen" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}

		ctx.writeEOF();

		// TODO real data here

		ctx.writeEOF();
		ctx.complete();
	}

	private void showColumns(ServerConnection c, String databaseName, String tableName, String pattern, boolean isFull) {
		LOGGER.info("showColumns: " + databaseName + " " + tableName + " " + pattern);
		if (databaseName == null) {
			c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
			return;
		}

		DatabaseProvider provider = lookup(DatabaseProvider.class, databaseName);

		if (provider == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Can not load database '" + databaseName + "'");
			return;
		}

		TableProvider[] tables = provider.getTables();
		TableProvider table = null;
		for (TableProvider t : tables) {
			if (t.getName().equals(tableName)) {
				table = t;
				break;
			}
		}
		ColumnMeta[] columns = null;
		if (table != null) {
			columns = table.getColumns();
		}

		CommandContext ctx = new CommandContext(c);
		String[] names = null;
		if (isFull) {
			names = new String[] { "Field", "Type", "Collation", "Null", "Key", "Default", "Extra", "Privileges", "Comment" };
		} else {
			names = new String[] { "Field", "Type", "Null", "Key", "Default", "Extra" };
		}
		ctx.writeHeader(names.length);
		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}
		ctx.writeEOF();

		if (columns != null) {
			for (ColumnMeta column : columns) {
				String dataType = TypeUtils.convertFieldTypeToString(TypeUtils.convertJavaTypeToFieldType(column.getType()));

				IndexMeta[] indexes = table.getIndexes();
				boolean isIndex = false;
				for (IndexMeta index : indexes) {
					for (int j = 0; j < index.getLength(); j++) {
						if (index.getColumn(j).equals(column)) {
							isIndex = true;
							break;
						}
					}
				}

				boolean isFilter = pattern != null ? SqlWildcard.like(column.getName(), pattern) : true;
				if (isFilter) {
					if (isFull) {
						ctx.writeRow(column.getName(), dataType, null, null, isIndex ? "PRI" : "", null, null,
						      "select,insert,update,references", null);
					} else {
						ctx.writeRow(column.getName(), dataType, null, isIndex ? "PRI" : "", null, null);
					}
				}
			}
		}

		ctx.writeEOF();
		ctx.complete();
	}

	/**
	 * @param c
	 */
	private void showDatabases(ServerConnection c) {
		LOGGER.info("showDatabases");
		CommandContext ctx = new CommandContext(c);
		String[] names = { "Database" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}

		ctx.writeEOF();

		List<DatabaseProvider> databases = null;
		try {
			databases = lookupList(DatabaseProvider.class);
		} catch (LookupException e) {
		}

		if (databases != null) {
			for (DatabaseProvider database : databases) {
				ctx.writeRow(database.getName());
			}
		}

		ctx.writeEOF();
		ctx.complete();
	}

	private void showIndexes(ServerConnection c, String databaseName, String tableName, String pattern) {
		LOGGER.info("showIndexes: " + databaseName + " " + tableName + " " + pattern);
		if (databaseName == null) {
			c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
			return;
		}

		DatabaseProvider provider = lookup(DatabaseProvider.class, databaseName);

		if (provider == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Can not load database '" + databaseName + "'");
			return;
		}

		TableProvider[] tables = provider.getTables();
		TableProvider table = null;
		for (TableProvider t : tables) {
			if (t.getName().equals(tableName)) {
				table = t;
				break;
			}
		}
		IndexMeta[] indexes = null;
		if (table != null) {
			indexes = table.getIndexes();
		}

		CommandContext ctx = new CommandContext(c);
		String[] names = new String[] { "Table", "Non_unique", "Key_name", "Seq_in_index", "Column_name", "Collation", "Cardinality",
		      "Sub_part", "Packed", "Null", "Index_type", "Comment", "Index_comment" };
		ctx.writeHeader(names.length);
		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}
		ctx.writeEOF();

		if (indexes != null) {
			for (IndexMeta index : indexes) {
				for (int i = 0; i < index.getLength(); i++) {
					boolean isFilter = pattern != null ? SqlWildcard.like(index.getColumn(i).getName(), pattern) : true;
					if (isFilter) {
						ctx.writeRow(table.getName(), "1", "PRIMARY", String.valueOf(1 + i), index.getColumn(i).getName(),
						      index.isAscend(i) ? "A" : "NULL", String.valueOf(((Enum<?>) index.getColumn(i)).ordinal()), "NULL", "NULL",
						      null, "BTREE", "", "");
					}
				}
			}
		}

		ctx.writeEOF();
		ctx.complete();
	}

	private void showStatus(ServerConnection c) {
		LOGGER.info("showStatus");
		Map<String, String> map = new HashMap<String, String>();

		map.put("SampleName", "SampleValue");
		// TODO real data here

		CommandContext ctx = new CommandContext(c);
		String[] names = { "Variable_name", "Value" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}

		ctx.writeEOF();

		for (Map.Entry<String, String> e : map.entrySet()) {
			String[] values = new String[names.length];
			int index = 0;

			values[index++] = e.getKey();
			values[index++] = e.getValue();
			ctx.writeRow(values);
		}

		ctx.writeEOF();
		ctx.complete();
	}

	private void showTables(ServerConnection c, String databaseName, String pattern, boolean isFull) {
		LOGGER.info("showTables: " + databaseName + " " + pattern);
		if (databaseName == null) {
			c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
			return;
		}

		DatabaseProvider provider = lookup(DatabaseProvider.class, databaseName);

		if (provider == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Can not load database '" + databaseName + "'");
			return;
		}

		CommandContext ctx = new CommandContext(c);
		String[] names = null;
		if (isFull) {
			names = new String[] { "Tables_in_" + databaseName, "Table_type" };
		} else {
			names = new String[] { "TABLE" };
		}
		ctx.writeHeader(names.length);
		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}
		ctx.writeEOF();

		TableProvider[] tables = provider.getTables();
		if (tables != null) {
			for (TableProvider table : tables) {
				boolean isFilter = pattern != null ? SqlWildcard.like(table.getName(), pattern) : true;
				if (isFilter) {
					if (isFull) {
						if (provider instanceof InformationSchemaDatabaseProvider) {
							ctx.writeRow(table.getName(), "SYSTEM TABLE");
						} else {
							ctx.writeRow(table.getName(), "BASE TABLE");
						}
					} else {
						ctx.writeRow(table.getName());
					}
				}
			}
		}

		ctx.writeEOF();
		ctx.complete();
	}

	private void showTableStatus(ServerConnection c, String databaseName, String pattern) {
		LOGGER.info("showTableStatus: " + databaseName + " " + pattern);
		if (databaseName == null) {
			error(c, ErrorCode.ER_NO_DB_ERROR, "No database specified");
			return;
		}

		DatabaseProvider provider = lookup(DatabaseProvider.class, databaseName);

		if (provider == null) {
			error(c, ErrorCode.ER_BAD_DB_ERROR, "Can not load database '%s'", databaseName);
			return;
		}

		TableProvider[] tables = provider.getTables();
		CommandContext ctx = new CommandContext(c);
		String[] names = { "Name", "Engine", "Version", "Row_format", "Rows", "Avg_row_length", "Data_length", "Max_data_length",
		      "Index_length", "Data_free", "Auto_increment", "Create_time", "Update_time", "Check_time", "Collation", "Checksum",
		      "Create_options", "Comment" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}

		ctx.writeEOF();

		for (TableProvider table : tables) {
			boolean isFilter = pattern != null ? SqlWildcard.like(table.getName(), pattern) : true;
			if (isFilter) {
				String[] values = new String[names.length];
				int index = 0;

				values[index++] = table.getName();
				values[index++] = "Bee";
				values[index++] = "10";
				ctx.writeRow(values);
			}
		}

		ctx.writeEOF();
		ctx.complete();
	}

	private void showVariables(ServerConnection c) {
		LOGGER.info("showVariables");
		Map<String, Object> metadata = m_sessionManager.getSession().getMetadata();

		CommandContext ctx = new CommandContext(c);
		String[] names = { "Variable_name", "Value" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VARCHAR);
		}

		ctx.writeEOF();

		for (Map.Entry<String, Object> e : metadata.entrySet()) {
			String[] values = new String[names.length];
			int index = 0;

			values[index++] = e.getKey();
			values[index++] = String.valueOf(e.getValue());
			ctx.writeRow(values);
		}

		ctx.writeEOF();
		ctx.complete();
	}
}
