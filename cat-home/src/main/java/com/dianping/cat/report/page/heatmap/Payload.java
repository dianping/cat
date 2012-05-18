package com.dianping.cat.report.page.heatmap;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("ne")
	private String m_ne;

	@FieldMeta("sw")
	private String m_sw;

	@FieldMeta("width")
	private String m_width;

	@FieldMeta("height")
	private String m_height;

	@FieldMeta("cb")
	private String m_cb;

	public Payload() {
		super(ReportPage.HEATMAP);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getHeight() {
		return m_height;
	}

	public String getNe() {
		return m_ne;
	}

	public String getSw() {
		return m_sw;
	}

	public String getWidth() {
		return m_width;
	}

	public void setAction(Action action) {
		m_action = action;
	}

	public void setHeight(String height) {
		m_height = height;
	}

	public void setNe(String ne) {
		m_ne = ne;
	}

	public void setSw(String sw) {
		m_sw = sw;
	}

	public void setWidth(String width) {
		m_width = width;
	}

	public String getCb() {
   	return m_cb;
   }

	public void setCb(String cb) {
   	m_cb = cb;
   }

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
