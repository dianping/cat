package com.dianping.cat.broker.api.page.batch;

import com.dianping.cat.broker.api.ApiPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ApiPage, Action> {
	private ApiPage m_page;

	@FieldMeta("op")
	private Action m_action;

   @FieldMeta("c")
   private String  m_content;

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
	
	public String getContent() {
   	return m_content;
   }

	public void setContent(String content) {
   	m_content = content;
   }

	@Override
	public void setPage(String page) {
		m_page = ApiPage.getByName(page, ApiPage.BATCH);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
