package com.dianping.bee.engine.spi.handler.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cobar.CobarServer;
import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.Fields;
import com.alibaba.cobar.config.model.SchemaConfig;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.response.ShowDatabases;
import com.dianping.bee.engine.spi.DatabaseProvider;
import com.dianping.bee.engine.spi.TableProvider;
import com.dianping.bee.engine.spi.handler.AbstractCommandHandler;

public class ShowHandler extends AbstractCommandHandler {
	@Override
	public void handle(ServerConnection c, List<String> parts) {
		int len = parts.size();
		String first = len > 0 ? parts.get(0) : null;
		String second = len > 1 ? parts.get(1) : null;
		String third = len > 2 ? parts.get(2) : null;
		String forth = len > 3 ? parts.get(3) : null;

		if ("databases".equalsIgnoreCase(first)) {
			ShowDatabases.response(c);
		} else if ("tables".equalsIgnoreCase(first)) {
			showTables(c);
		} else if ("table".equalsIgnoreCase(first)) {
			if ("status".equalsIgnoreCase(second) && "from".equalsIgnoreCase(third)) {
				showTableStatus(c, unescape(forth));
			}
		} else if ("status".equalsIgnoreCase(first)) {
			showStatus(c);
		} else if ("variables".equalsIgnoreCase(first)) {
			showVariables(c);
		} else if ("collation".equalsIgnoreCase(first)){
			showCollation(c);
		}
		else {
			error(c, ErrorCode.ER_UNKNOWN_COM_ERROR, "Unsupported show command");
		}
	}

	/**
	 * @param c
	 */
   private void showCollation(ServerConnection c) {
   	Map<String,String> map = new HashMap<String,String>();
   	
		CommandContext ctx = new CommandContext(c);
		String[] names = { "Collation", "Charset", "Id", "Default", "Compiled", "Sortlen" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
		}

		ctx.writeEOF();
		
		// TODO real data here
		
		ctx.writeEOF();
		ctx.complete();
   }

	private void showStatus(ServerConnection c) {
		Map<String, String> map = new HashMap<String, String>();

		map.put("SampleName", "SampleValue");
		// TODO real data here

		CommandContext ctx = new CommandContext(c);
		String[] names = { "Variable_name", "Value" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
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

	private void showTables(ServerConnection c) {
		// 检查当前使用的DB
		String db = c.getSchema();
		if (db == null) {
			c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
			return;
		}

		SchemaConfig schema = CobarServer.getInstance().getConfig().getSchemas().get(db);
		if (schema == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
			return;
		}

		DatabaseProvider provider = null;
		try {
			provider = lookup(DatabaseProvider.class, db);
		} catch (Exception e) {
			// ignore it
		}

		if (provider == null) {
			c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Can not load database '" + db + "'");
			return;
		}

		TableProvider[] tables = provider.getTables();
		CommandContext ctx = new CommandContext(c);

		ctx.writeHeader(1);
		ctx.writeField("TABLE", Fields.FIELD_TYPE_VAR_STRING);
		ctx.writeEOF();

		for (TableProvider table : tables) {
			ctx.writeRow(table.getName());
		}

		ctx.writeEOF();
		ctx.complete();
	}

	private void showTableStatus(ServerConnection c, String dbName) {
		if (dbName == null) {
			error(c, ErrorCode.ER_NO_DB_ERROR, "No database specified");
			return;
		}

		DatabaseProvider provider = null;

		try {
			provider = lookup(DatabaseProvider.class, dbName);
		} catch (Exception e) {
			error(c, ErrorCode.ER_BAD_DB_ERROR, "Can not load database '%s'", dbName);
			return;
		}

		TableProvider[] tables = provider.getTables();
		CommandContext ctx = new CommandContext(c);
		String[] names = { "Name", "Engine", "Version", "Row_format", "Rows", "Avg_row_length", "Data_length", "Max_data_length",
		      "Index_length", "Data_free", "Auto_increment", "Create_time", "Update_time", "Check_time", "Collation", "Checksum",
		      "Create_options", "Comment" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
		}

		ctx.writeEOF();

		for (TableProvider table : tables) {
			String[] values = new String[names.length];
			int index = 0;

			values[index++] = table.getName();
			values[index++] = "Bee";
			values[index++] = "1.0";
			ctx.writeRow(values);
		}

		ctx.writeEOF();
		ctx.complete();
	}

	private void showVariables(ServerConnection c) {
		Map<String, String> map = new HashMap<String, String>();

		map.put("BeeStatus", "Good");
		// TODO real data here

		CommandContext ctx = new CommandContext(c);
		String[] names = { "Variable_name", "Value" };

		ctx.writeHeader(names.length);

		for (String name : names) {
			ctx.writeField(name, Fields.FIELD_TYPE_VAR_STRING);
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
}
