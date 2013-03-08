package com.dianping.cat.report.page.heatmap;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("cb")
	private String m_cb;

	@FieldMeta("display")
	private String m_display;

	@FieldMeta("flag")
	private int m_flag;

	@FieldMeta("height")
	private int m_height;

	@FieldMeta("lat1")
	private double m_lat1;

	@FieldMeta("lat2")
	private double m_lat2;

	@FieldMeta("lng1")
	private double m_lng1;

	@FieldMeta("lng2")
	private double m_lng2;

	@FieldMeta("unit")
	private int m_unit = 20;

	@FieldMeta("width")
	private int m_width;

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

	public String getDisplay() {
		return m_display;
	}

	public int getFlag() {
		return m_flag;
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

	public void setDisplay(String display) {
		m_display = display;
	}

	public void setFlag(int flag) {
		m_flag = flag;
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

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
