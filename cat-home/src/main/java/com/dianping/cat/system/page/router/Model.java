package com.dianping.cat.system.page.router;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<SystemPage, Action, Context> {
	
	private String m_content;
	
	public String getContent() {
   	return m_content;
   }

	public void setContent(String content) {
   	m_content = content;
   }

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.API;
	}
}
