package com.dianping.cat.report.page.problem;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("ip")
	private String m_ipAddress;

	@FieldMeta("group")
	private String m_groupName;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("minute")
	private int m_minute;

	@FieldMeta("url")
	private boolean m_url;

	@FieldMeta("sql")
	private boolean m_sql;

	@FieldMeta("tran")
	private boolean m_tran;

	@FieldMeta("cache")
	private boolean m_cache;

	@FieldMeta("call")
	private boolean m_call;
	
	@FieldMeta("error")
	private boolean m_error;

	@FieldMeta("longUrl")
	private boolean m_longUrl;

	@FieldMeta("longTime")
	private int m_longTime;
	
	public Payload() {
		super(ReportPage.PROBLEM);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public int getMinute() {
		return m_minute;
	}
		
	public String getThreadId() {
		return m_threadId;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.GROUP);
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public String getGroupName() {
   	return m_groupName;
   }

	public void setGroupName(String groupName) {
   	m_groupName = groupName;
   }
	
	public boolean isUrl() {
   	return m_url;
   }

	public void setUrl(boolean url) {
   	m_url = url;
   }

	public boolean isSql() {
   	return m_sql;
   }

	public void setSql(boolean sql) {
   	m_sql = sql;
   }

	public boolean isTran() {
   	return m_tran;
   }

	public void setTran(boolean tran) {
   	m_tran = tran;
   }

	public boolean isCache() {
   	return m_cache;
   }

	public void setCache(boolean cache) {
   	m_cache = cache;
   }

	public boolean isError() {
   	return m_error;
   }

	public void setError(boolean error) {
   	m_error = error;
   }

	public boolean isLongUrl() {
   	return m_longUrl;
   }

	public void setLongUrl(boolean longUrl) {
   	m_longUrl = longUrl;
   }

	public int getLongTime() {
   	return m_longTime;
   }

	public void setLongTime(int longTime) {
   	m_longTime = longTime;
   }
	
	public boolean isCall() {
   	return m_call;
   }

	public void setCall(boolean call) {
   	m_call = call;
   }

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.GROUP;
		}
	}
}
