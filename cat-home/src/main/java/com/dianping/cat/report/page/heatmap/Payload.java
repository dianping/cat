package com.dianping.cat.report.page.heatmap;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("lat1")
	private double m_lat1;

	@FieldMeta("lat2")
	private double m_lat2;

	@FieldMeta("lng1")
	private double m_lng1;

	@FieldMeta("lng2")
	private double m_lng2;

	@FieldMeta("width")
	private int m_width;

	@FieldMeta("height")
	private int m_height;

	@FieldMeta("unit")
	private int m_unit = 20;

	@FieldMeta("cb")
	private String m_cb;

	@FieldMeta("flag")
	private int m_flag;

	@FieldMeta("display")
	private String m_display;

	public Payload() {
		super(ReportPage.HEATMAP);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getCb() {
		return m_cb;
	}

	public int getHeight() {
		return m_height;
	}

	public double getLat1() {
		return m_lat1;
	}

	public double getLat2() {
		return m_lat2;
	}

	public double getLng1() {
		return m_lng1;
	}

	public double getLng2() {
		return m_lng2;
	}

	public int getUnit() {
		return m_unit;
	}

	public int getWidth() {
		return m_width;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setCb(String cb) {
		m_cb = cb;
	}

	public void setHeight(int height) {
		m_height = height;
	}

	public void setLat1(double lat1) {
		m_lat1 = lat1;
	}

	public void setLat2(double lat2) {
		m_lat2 = lat2;
	}

	public void setLng1(double lng1) {
		m_lng1 = lng1;
	}

	public void setLng2(double lng2) {
		m_lng2 = lng2;
	}

	public void setUnit(int unit) {
		m_unit = unit;
	}

	public void setWidth(int width) {
		m_width = width;
	}
	
	public int getFlag() {
   	return m_flag;
   }

	public void setFlag(int flag) {
   	m_flag = flag;
   }

	public String getDisplay() {
   	return m_display;
   }

	public void setDisplay(String display) {
   	m_display = display;
   }

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
