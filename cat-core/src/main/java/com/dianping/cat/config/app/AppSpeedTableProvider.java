package com.dianping.cat.config.app;

import java.util.Map;

import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.app.AppSpeedData;

@Named(type = TableProvider.class, value = AppSpeedTableProvider.LOGIC_TABLE_NAME)
public class AppSpeedTableProvider implements TableProvider {

	public final static String LOGIC_TABLE_NAME = "app-speed-data";

	protected String m_logicalTableName = "app-speed-data";

	private String m_physicalTableName = "app_speed_data";

	private String m_dataSourceName = "cat";

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return m_dataSourceName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		AppSpeedData data = (AppSpeedData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + data.getSpeedId();
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}
