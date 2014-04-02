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

   @FieldMeta("age")
   private int m_age = 0;
   
   @Override
   public Action getAction() {
   	if(m_action == null){
   		return Action.QUERYALL;
   	}else{
         return m_action;
   	}
   }

	public int getAge() {
		return m_age;
	}

	public String getName() {
		return m_name;
	}

	@Override
   public ReportPage getPage() {
      return m_page;
   }

	public void setAction(String action) {
      m_action = Action.getByName(action, Action.QUERYALL);
   }

   public void setAge(int age) {
		m_age = age;
	}

   public void setName(String name) {
		m_name = name;
	}

   @Override
   public void setPage(String page) {
      m_page = ReportPage.getByName(page, ReportPage.TEST);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.QUERYALL;
      }
   }
}
