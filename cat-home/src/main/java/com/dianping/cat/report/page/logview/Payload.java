package com.dianping.cat.report.page.logview;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.storage.TagThreadSupport.Direction;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;
import com.site.web.mvc.payload.annotation.PathMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("header")
	private boolean m_showHeader = true;

	@PathMeta("path")
	private String[] m_path;

	@FieldMeta("tag1")
	private String m_tag1;

	@FieldMeta("tag2")
	private String m_tag2;

	@Override
	public Action getAction() {
		return m_action;
	}

	public Direction getDirection() {
		if (m_tag1 != null) {
			return Direction.BACKWARD;
		} else if (m_tag2 != null) {
			return Direction.FORWARD;
		} else {
			return null;
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String[] getPath() {
		return m_path;
	}

	public String getTag() {
		if (m_tag1 != null) {
			return m_tag1;
		} else if (m_tag2 != null) {
			return m_tag2;
		} else {
			return null;
		}
	}

	public boolean isShowHeader() {
		return m_showHeader;
	}

	public void setAction(Action action) {
		m_action = action;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.LOGVIEW);
	}

	public void setPath(String[] path) {
		m_path = path;
	}

	public void setShowHeader(String showHeader) {
		m_showHeader = !"no".equals(showHeader);
	}

	public void setTag1(String tag1) {
		m_tag1 = tag1;
	}

	public void setTag2(String tag2) {
		m_tag2 = tag2;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
