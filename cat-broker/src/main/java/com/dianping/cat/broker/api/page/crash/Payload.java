package com.dianping.cat.broker.api.page.crash;

import com.dianping.cat.broker.api.ApiPage;

import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ApiPage, Action> {
	private ApiPage m_page;

	@FieldMeta("op")
	private Action m_action;

	public static int ANDRIOD = 1;

	public static int IPHONE = 2;

	@FieldMeta("mt")
	private int m_mobileType = ANDRIOD;

	@FieldMeta("av")
	private String m_appVersion;

	@FieldMeta("pv")
	private String m_plateformVersion;

	@FieldMeta("m")
	private String m_module = "Default";

	@FieldMeta("msg")
	private String m_message = "ERROR";

	@FieldMeta("l")
	private String m_level = "Default";

	@FieldMeta("d")
	private String m_detail;

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getAppVersion() {
		return m_appVersion;
	}

	public String getDetail() {
		return m_detail;
	}

	public String getLevel() {
		return m_level;
	}

	public String getMessage() {
		return m_message;
	}

	public int getMobileType() {
		return m_mobileType;
	}

	public String getModule() {
		return m_module;
	}

	@Override
	public ApiPage getPage() {
		return m_page;
	}

	public String getPlateformVersion() {
		return m_plateformVersion;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setAppVersion(String appVersion) {
		m_appVersion = appVersion;
	}

	public void setDetail(String detail) {
		m_detail = detail;
	}

	public void setLevel(String level) {
		if (StringUtils.isNotEmpty(level)) {
			m_level = level;
		}
	}

	public void setMessage(String message) {
		if (StringUtils.isNotEmpty(message)) {
			m_message = message;
		}
	}

	public void setMobileType(int mobileType) {
		m_mobileType = mobileType;
	}

	public void setModule(String module) {
		if (StringUtils.isNotEmpty(module)) {
			m_module = module;
		}
	}

	@Override
	public void setPage(String page) {
		m_page = ApiPage.getByName(page, ApiPage.CRASH);
	}

	public void setPlateformVersion(String plateformVersion) {
		m_plateformVersion = plateformVersion;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
