package com.dianping.cat.report.page.app;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
   private ReportPage m_page;

	@FieldMeta("op")
   private Action m_action;
	
	@FieldMeta("query1")
   private String m_query1;
	
	@FieldMeta("query2")
   private String m_query2;

   public Payload() {
   	super(ReportPage.APP);
   }

   @Override
   public Action getAction() {
      return m_action;
   }

   @Override
   public ReportPage getPage() {
      return m_page;
   }

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
   }

   @Override
   public void setPage(String page) {
      m_page = ReportPage.getByName(page, ReportPage.APP);
   }
   
   public String getQuery1() {
   	return m_query1;
   }

	public void setQuery1(String query1) {
   	m_query1 = query1;
   }

	public String getQuery2() {
   	return m_query2;
   }

	public void setQuery2(String query2) {
   	m_query2 = query2;
   }

	@Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
}
