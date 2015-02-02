package com.dianping.cat.system;

import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.annotation.ModuleMeta;

public enum SystemPage implements Page {

   LOGIN("login", "login", "Login", "Login", false),

   CONFIG("config", "config", "Config", "Config", false),

   PLUGIN("plugin", "plugin", "Plugin", "Plugin", true),

   ROUTER("router", "router", "Router", "Router", true);

   private String m_name;

   private String m_path;

   private String m_title;

   private String m_description;

   private boolean m_standalone;

   private SystemPage(String name, String path, String title, String description, boolean standalone) {
      m_name = name;
      m_path = path;
      m_title = title;
      m_description = description;
      m_standalone = standalone;
   }

   public static SystemPage getByName(String name, SystemPage defaultPage) {
      for (SystemPage action : SystemPage.values()) {
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
      ModuleMeta meta = SystemModule.class.getAnnotation(ModuleMeta.class);

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

   public SystemPage[] getValues() {
      return SystemPage.values();
   }
}
