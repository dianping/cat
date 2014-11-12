package com.dianping.cat.service.app.speed;

import java.util.Map;

import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;

import com.dianping.cat.app.AppDataCommand;

public class AppSpeedTableProvider implements TableProvider {
	private String m_logicalTableName = "app-speed_data";

	private String m_physicalTableName = "app_speed_data";

	private String m_dataSourceName = "app_speed";

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
		AppDataCommand command = (AppDataCommand) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + command.getCommandId();
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}