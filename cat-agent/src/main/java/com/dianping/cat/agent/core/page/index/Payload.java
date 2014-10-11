package com.dianping.cat.agent.core.page.index;

import com.dianping.cat.agent.core.CorePage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<CorePage, Action> {
   private CorePage m_page;

   @FieldMeta("op")
   private Action m_action;

   @Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public CorePage getPage() {
      return m_page;
   }

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public void setPage(String page) {
      m_page = CorePage.getByName(page, CorePage.INDEX);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
}
