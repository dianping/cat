package com.dianping.cat.config.web;

import com.dianping.cat.web.AjaxData;
import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

import java.util.Map;

@Named(type = TableProvider.class, value = AjaxDataTableProvider.LOGIC_TABLE_NAME)
public class AjaxDataTableProvider implements TableProvider {
	public final static String LOGIC_TABLE_NAME = "ajax-data";

	protected String m_logicalTableName = "ajax-data";

	private String m_physicalTableName = "ajax_data";

	private String m_dataSourceName = "cat";

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return m_dataSourceName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		AjaxData webApiData = (AjaxData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + webApiData.getApiId();
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}
