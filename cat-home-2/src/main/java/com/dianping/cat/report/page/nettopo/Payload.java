package com.dianping.cat.report.page.nettopo;

import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
   private ReportPage m_page;

   @FieldMeta("op")
   private Action m_action;

   public void setAction(String action) {
      m_action = Action.getByName(action, Action.VIEW);
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
      m_page = ReportPage.getByName(page, ReportPage.NETTOPO);
   }

   @Override
   public void validate(ActionContext<?> ctx) {
      if (m_action == null) {
         m_action = Action.VIEW;
      }
   }
   
   @FieldMeta("date")
   private String m_date;
   
   public String getDate() {
	   return m_date;
   }
   
   public void setDate(String date) {
	   m_date = date;
   }
   
   @FieldMeta("reportType")
   private String m_reportType;
   
   //@Override
   public String getReportType() {
	   return m_reportType;
   }
 		
   public void setReportType(String reportType) {
	   m_reportType = reportType;
   }
}
