package com.dianping.cat.report;

import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

@Named(type = TableProvider.class, value = HourlyReportTableProvider.LOGIC_TABLE_NAME)
public class HourlyReportTableProvider implements TableProvider, Initializable {

	public final static String LOGIC_TABLE_NAME = "report";

	protected String m_logicalTableName = LOGIC_TABLE_NAME;

	private String m_dataSourceName = "cat";

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return m_dataSourceName;
	}

	@Override
	public void initialize() throws InitializationException {
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		return "hourlyreport";
	}

}