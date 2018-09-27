package com.dianping.cat.config.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.app.crash.CrashLog;

@Named(type = TableProvider.class, value = CrashLogTableProvider.LOGIC_TABLE_NAME)
public class CrashLogTableProvider implements TableProvider, Initializable {

	public final static String LOGIC_TABLE_NAME = "crash-log";

	private String m_physicalTableName = "crash_log";

	private String m_originDataSource = "app";

	private String m_newDataSource = "app_crash";

	private Date m_historyDate;

	@Override
	public void initialize() throws InitializationException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		try {
			m_historyDate = sdf.parse("2016-06-18 00:00");
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	@Override
	public String getDataSourceName(Map<String, Object> hints, String logicalTableName) {
		CrashLog crashLog = (CrashLog) hints.get(QueryEngine.HINT_DATA_OBJECT);
		Date startTime = crashLog.getStartTime();
		Date updatetime = crashLog.getUpdatetime();

		if ((startTime != null && startTime.before(m_historyDate)) || crashLog.getKeyId() >= 8845323
		      || (updatetime != null && updatetime.before(m_historyDate))) {
			return m_originDataSource;
		} else {
			return m_newDataSource;
		}
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints, String logicalTableName) {
		return m_physicalTableName;
	}

}
