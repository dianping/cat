package com.dianping.cat.report;

import com.site.web.mvc.Page;
import com.site.web.mvc.annotation.ModuleMeta;

public enum ReportPage implements Page {

   HOME("home", "home", "Home", true),

   TRANSACTION("transaction", "t", "Transaction", true),

   FAILURE("failure", "f", "Failure", true),

   LOGVIEW("logview", "m", "Logview", true);

   private String m_name;

   private String m_path;

   private String m_description;

   private boolean m_realPage;

   private ReportPage(String name, String path, String description, boolean realPage) {
      m_name = name;
      m_path = path;
      m_description = description;
      m_realPage = realPage;
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

   public boolean isRealPage() {
      return m_realPage;
   }

   public ReportPage[] getValues() {
      return ReportPage.values();
   }
}
