package com.dianping.cat.report.page.test;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
   private ReportPage m_page;

   @FieldMeta("op")
   private Action m_action;
   
   @FieldMeta("name")
   private String m_name;
   
   
   public void setAction(String action) {
      m_action = Action.getByName(action, Action.INSERT);
   }

   public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public ReportPage getPage() {
      return m_page;
   }

   @Override
   public void setPage(String page) {
      m_page = ReportPage.getByName(page, ReportPage.TEST);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.INSERT;
      }
   }
}
