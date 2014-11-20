package com.dianping.cat.config.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;

import com.dianping.cat.app.AppCommandData;

public class AppCommandDataTableProvider implements TableProvider {

	private String m_logicalTableName = "app-command-data";

	private String m_physicalTableName = "app_command_data";

	private String m_oldPhysicalTableName = "app_data_command";

	private String m_dataSourceName = "app";

	@Override
	public String getDataSourceName(Map<String, Object> hints) {
		return m_dataSourceName;
	}

	@Override
	public String getLogicalTableName() {
		return m_logicalTableName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints) {
		AppCommandData command = (AppCommandData) hints.get(QueryEngine.HINT_DATA_OBJECT);
		Date period = command.getPeriod();
		Date old = new Date();

		try {
			old = new SimpleDateFormat("yyyy-MM-dd").parse("2014-11-20");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		if (period.before(old)) {
			return m_oldPhysicalTableName + "_" + command.getCommandId();
		} else {
			return m_physicalTableName + "_" + command.getCommandId();
		}
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}