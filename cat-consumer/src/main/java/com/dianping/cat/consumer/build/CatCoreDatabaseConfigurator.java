package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;

final class CatCoreDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(defineJdbcDataSourceComponent("cat", "com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:3306/cat", "root", "password", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

      defineSimpleTableProviderComponents(all, "cat", com.dainping.cat.consumer.core.dal._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dainping.cat.consumer.core.dal._INDEX.getDaoClasses());

      return all;
   }
}
