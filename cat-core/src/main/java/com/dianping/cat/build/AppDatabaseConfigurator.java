package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public final class AppDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      // all.add(defineJdbcDataSourceComponent("app", "com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/cat", "root", "***", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

      defineSimpleTableProviderComponents(all, "app", com.dianping.cat.app._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.cat.app._INDEX.getDaoClasses());

      return all;
   }
}
