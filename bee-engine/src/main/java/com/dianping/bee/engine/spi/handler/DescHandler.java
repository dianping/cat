package com.dianping.bee.engine.spi.handler;

import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.helper.SqlParsers;
import com.dianping.bee.engine.helper.TypeUtils;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.site.lookup.annotation.Inject;

public class DescHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(DescHandler.class);

	@Inject
	private TableProviderManager m_manager;

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
		String tableName = SqlParsers.forEscape().unescape(parts.get(0));
		LOGGER.info("DESC : " + tableName);

		String db = c.getSchema();
		if (db == null) {
			error(c, ErrorCode.ER_NO_DB_ERROR, "No database selected");
			return;
		}

		SchemaConfig schema = CobarServer.getInstance().getConfig().getSchemas().get(db);
		if (schema == null) {
			error(c, ErrorCode.ER_BAD_DB_ERROR, "Unknown database '%s'", db);
			return;
		}

		TableProvider table = m_manager.getTableProvider(db, tableName);
		if (table == null) {
			error(c, ErrorCode.ER_BAD_TABLE_ERROR, "Unknown table '%s'", tableName);
			return;
		}

		ColumnMeta[] columns = table.getColumns();
		CommandContext ctx = new CommandContext(c);
		String[] names = { "Field", "Type", "Null", "Key", "Default", "Extra" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
		}

		ctx.writeEOF();

		for (ColumnMeta column : columns) {
			String[] values = new String[names.length];
			int index = 0;

			values[index++] = column.getName();
			values[index++] = TypeUtils.convertFieldTypeToString(TypeUtils.convertJavaTypeToFieldType(column.getType()));
			ctx.writeRow(values);
		}

		ctx.writeEOF();
		ctx.complete();
	}
}
