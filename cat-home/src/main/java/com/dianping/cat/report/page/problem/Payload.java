package com.dianping.cat.report.page.problem;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("group")
	private String m_groupName;

	@FieldMeta("linkCount")
	private int m_linkCount;

	@FieldMeta("threshold")
	private int m_longTime;

	@FieldMeta("minute")
	private int m_minute;

	@FieldMeta("sqlThreshold")
	private int m_sqlLongTime;
	
	@FieldMeta("serviceThreshold")
	private int m_seviceLongTime;

	@FieldMeta("status")
	private String m_status;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("type")
	private String m_type;

	public Payload() {
		super(ReportPage.PROBLEM);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getGroupName() {
		return m_groupName;
	}

	public int getLinkCount() {
		if (m_linkCount < 40) {
			m_linkCount = 40;
		}
		return m_linkCount;
	}

	public int getLongTime() {
		if (m_longTime == 0) {
			m_longTime = 1000;
		}
		return m_longTime;
	}

	public int getMinute() {
		return m_minute;
	}

	public int getRealLongTime() {
		return m_longTime;
	}

	public int getSeviceLongTime() {
		return m_seviceLongTime;
	}

	public int getSqlLongTime() {
		return m_sqlLongTime;
	}

	public String getStatus() {
		return m_status;
	}

	public String getThreadId() {
		return m_threadId;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setGroupName(String groupName) {
		m_groupName = groupName;
	}

	public void setLinkCount(int linkSize) {
		m_linkCount = linkSize;
	}

	public void setLongTime(int longTime) {
		m_longTime = longTime;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public void setSeviceLongTime(int seviceLongTime) {
		m_seviceLongTime = seviceLongTime;
	}

	public void setSqlLongTime(int sqlLongTime) {
		m_sqlLongTime = sqlLongTime;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	public void setThreadId(String threadId) {
		m_threadId = threadId;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
