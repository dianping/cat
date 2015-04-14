package com.dianping.cat.config.app;

import java.util.Map;

import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;

import com.dianping.cat.app.AppConnectionData;

public class AppConnectionTableProvider implements TableProvider {

	private String m_logicalTableName = "app-connection-data";

	private String m_physicalTableName = "app_connection_data";

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
		AppConnectionData data = (AppConnectionData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + data.getCommandId();
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}