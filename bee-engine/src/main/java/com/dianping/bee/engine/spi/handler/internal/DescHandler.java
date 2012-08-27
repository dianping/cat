package com.dianping.bee.engine.spi.handler.internal;

import java.util.List;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.server.ServerConnection;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.TableProviderManager;
import com.dianping.bee.engine.spi.handler.AbstractCommandHandler;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.internal.TypeUtils;
import com.site.lookup.annotation.Inject;

public class DescHandler extends AbstractCommandHandler {
	@Inject
	private TableProviderManager m_manager;

	@Override
	public void handle(ServerConnection c, List<String> parts) {
		String tableName = unescape(parts.get(0));

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

		TableProvider table = m_manager.getTableProvider(tableName);
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
