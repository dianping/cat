package com.dianping.cat.config.app;

import java.util.Map;

import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.app.AppConnectionData;

@Named(type = TableProvider.class, value = AppConnectionTableProvider.LOGIC_TABLE_NAME)
public class AppConnectionTableProvider implements TableProvider {

	public final static String LOGIC_TABLE_NAME = "app-connection-data";

	protected String m_logicalTableName = "app-connection-data";

	private String m_physicalTableName = "app_connection_data";

	private String m_dataSourceName = "app";

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return m_dataSourceName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
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