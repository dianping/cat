package com.dianping.cat.broker.api.page.js;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.broker.api.ApiPage;

public class Payload implements ActionPayload<ApiPage, Action> {
	private ApiPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("timestamp")
	private long m_timestamp;

	@FieldMeta("error")
	private String m_error;

	@FieldMeta("file")
	private String m_file;

	@FieldMeta("line")
	private String m_line;

	@FieldMeta("data")
	private String m_data;

	public long getTimestamp() {
		return m_timestamp;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}

	public String getError() {
		return m_error;
	}

	public void setError(String error) {
		m_error = error;
	}

	public String getFile() {
		return m_file;
	}

	public void setFile(String file) {
		m_file = file;
	}

	public String getLine() {
		return m_line;
	}

	public void setLine(String line) {
		m_line = line;
	}

	public String getData() {
		return m_data;
	}

	public void setData(String data) {
		m_data = data;
	}

	public void setPage(ApiPage page) {
		m_page = page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ApiPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ApiPage.getByName(page, ApiPage.JS);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
