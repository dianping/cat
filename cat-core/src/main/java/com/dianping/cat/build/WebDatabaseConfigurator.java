package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

public final class WebDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      // all.add(defineJdbcDataSourceComponent("web", "com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/WebDataComm", "root", "***", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

      defineSimpleTableProviderComponents(all, "web", com.dianping.cat.web._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.cat.web._INDEX.getDaoClasses());

      return all;
   }
}
