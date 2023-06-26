package com.dianping.cat.config.app;

import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.app.AppCommandData;

@Named(type = TableProvider.class, value = AppCommandTableProvider.LOGIC_TABLE_NAME)
public class AppCommandTableProvider implements TableProvider, Initializable {

	@Inject
	private AppCommandConfigManager m_appCommandConfigManager;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public final static String LOGIC_TABLE_NAME = "app-command-data";

	protected String m_logicalTableName = "app-command-data";

	private String m_physicalTableName = "app_command_data";

	private String m_dataSourceName = "cat";

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return m_dataSourceName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		AppCommandData command = (AppCommandData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + command.getCommandId();
	}

	@Override
	public void initialize() throws InitializationException {
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}
}
