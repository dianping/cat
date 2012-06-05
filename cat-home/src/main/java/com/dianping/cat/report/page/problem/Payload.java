package com.dianping.cat.report.page.problem;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("group")
	private String m_groupName;

	@FieldMeta("thread")
	private String m_threadId;

	@FieldMeta("minute")
	private int m_minute;

	@FieldMeta("threshold")
	private int m_longTime;

	@FieldMeta("linkCount")
	private int m_linkCount;

	public Payload() {
		super(ReportPage.PROBLEM);
	}

	@Override
	public Action getAction() {
		return m_action;
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

	public int getLongTime() {
		if (m_longTime == 0) {
			m_longTime = 1000;
		}
		return m_longTime;
	}

	public int getRealLongTime() {
		return m_longTime;
	}

	public void setLongTime(int longTime) {
		m_longTime = longTime;
	}

	public int getLinkCount() {
		if (m_linkCount < 40) {
			m_linkCount = 40;
		}
		return m_linkCount;
	}

	public void setLinkCount(int linkSize) {
		m_linkCount = linkSize;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.ALL;
		}
	}
}
