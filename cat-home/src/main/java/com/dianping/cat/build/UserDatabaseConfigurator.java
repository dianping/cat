package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class UserDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(defineJdbcDataSourceComponent("user", "com.mysql.jdbc.Driver", "jdbc:mysql://192.168.8.44:3306/hawk", "hawk", "hawk", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

      defineSimpleTableProviderComponents(all, "user", com.dainping.cat.home.dal.user._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dainping.cat.home.dal.user._INDEX.getDaoClasses());

      return all;
   }
}
