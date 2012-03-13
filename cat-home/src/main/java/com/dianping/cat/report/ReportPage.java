package com.dianping.cat.report;

import com.site.web.mvc.Page;
import com.site.web.mvc.annotation.ModuleMeta;

public enum ReportPage implements Page {

   HOME("home", "home", "Home", "CAT Home Page.", true),

   PROBLEM("problem", "p", "Problem", "Problem discovered", true),

   LOGVIEW("logview", "m", "Logview", "CAT log details view for a given message.", false),
   
   TRANSACTION("transaction", "t", "Transaction", "Transaction summary report", true),

   IP("ip", "ip", "Top IP List", "Top visited IP addresses", true),

   MODEL("model", "model", "Model", "Model Service", false),

   SQL("sql", "sql", "SQL", "SQL Report", true);

   private String m_name;

   private String m_path;

   private String m_title;

   private String m_description;

   private boolean m_standalone;

   private ReportPage(String name, String path, String title, String description, boolean standalone) {
      m_name = name;
      m_path = path;
      m_title = title;
      m_description = description;
      m_standalone = standalone;
   }

   public static ReportPage getByName(String name, ReportPage defaultPage) {
      for (ReportPage action : ReportPage.values()) {
         if (action.getName().equals(name)) {
            return action;
         }
      }

      return defaultPage;
   }

   public String getDescription() {
      return m_description;
   }

   public String getModuleName() {
      ModuleMeta meta = ReportModule.class.getAnnotation(ModuleMeta.class);

      if (meta != null) {
         return meta.name();
      } else {
         return null;
      }
   }

   @Override
   public String getName() {
      return m_name;
   }

   @Override
   public String getPath() {
      return m_path;
   }

   public String getTitle() {
      return m_title;
   }

   public boolean isStandalone() {
      return m_standalone;
   }

   public ReportPage[] getValues() {
      return ReportPage.values();
   }
}
