package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public final class AppCrashDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      // all.add(defineJdbcDataSourceComponent("app_crash", "com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/cat", "root", "***", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

      defineSimpleTableProviderComponents(all, "app_crash", com.dianping.cat.app.crash._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.cat.app.crash._INDEX.getDaoClasses());

      return all;
   }
}
