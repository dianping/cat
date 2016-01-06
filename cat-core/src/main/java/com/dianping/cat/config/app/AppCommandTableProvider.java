package com.dianping.cat.config.app;

import java.util.Map;

import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;

import com.dianping.cat.app.AppCommandData;

public class AppCommandTableProvider implements TableProvider {
	private String m_physicalTableName = "app_command_data";

	private String m_dataSourceName = "app";

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return m_dataSourceName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		AppCommandData command = (AppCommandData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + command.getCommandId();
	}

   public void setDataSourceName(String dataSourceName) {
      m_dataSourceName = dataSourceName;
   }

   public void setPhysicalTableName(String physicalTableName) {
      m_physicalTableName = physicalTableName;
   }
}