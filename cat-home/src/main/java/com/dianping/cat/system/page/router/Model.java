package com.dianping.cat.system.page.router;

import org.unidal.web.mvc.ViewModel;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.system.SystemPage;

@ModelMeta("model")
public class Model extends ViewModel<SystemPage, Action, Context> {

	private String m_content;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	@Override
	public Action getDefaultAction() {
		return Action.API;
	}
}
