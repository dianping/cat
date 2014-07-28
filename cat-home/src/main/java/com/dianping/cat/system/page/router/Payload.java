package com.dianping.cat.system.page.router;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
   private SystemPage m_page;

   @FieldMeta("op")
   private Action m_action;
   
   @FieldMeta("domain")
   private String m_domain;
   
   @FieldMeta("date")
   private String m_date;
   
   public String getDomain() {
   	return m_domain;
   }

	public void setDomain(String domain) {
   	m_domain = domain;
   }

	public String getDate() {
   	return m_date;
   }

	public void setDate(String date) {
   	m_date = date;
   }

	public void setAction(String action) {
      m_action = Action.getByName(action, Action.API);
   }

   @Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public SystemPage getPage() {
      return m_page;
   }

   @Override
   public void setPage(String page) {
      m_page = SystemPage.getByName(page, SystemPage.ROUTER);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.API;
      }
   }
}
