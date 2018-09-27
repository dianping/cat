package com.dianping.cat.report;

import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

@Named(type = TableProvider.class, value = HourlyReportContentTableProvider.LOGIC_TABLE_NAME)
public class HourlyReportContentTableProvider implements TableProvider, Initializable {

	public final static String LOGIC_TABLE_NAME = "report-content";

	protected String m_logicalTableName = LOGIC_TABLE_NAME;

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		return "cat";
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		return "hourly_report_content";
	}

	@Override
	public void initialize() throws InitializationException {
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}