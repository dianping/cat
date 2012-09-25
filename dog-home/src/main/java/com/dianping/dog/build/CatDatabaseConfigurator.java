package com.dianping.dog.build;

import java.util.ArrayList;
import java.util.List;

import com.site.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import com.site.lookup.configuration.Component;

final class CatDatabaseConfigurator extends AbstractJdbcResourceConfigurator {
   @Override
   public List<Component> defineComponents() {
      List<Component> all = new ArrayList<Component>();

      all.add(defineJdbcDataSourceConfigurationManagerComponent("datasources.xml"));
      all.add(defineJdbcDataSourceComponent("cat", "com.mysql.jdbc.Driver", "jdbc:mysql://192.168.7.43:3306/cat", "dpcom_cat", "password", "<![CDATA[useUnicode=true&autoReconnect=true]]>"));

      defineSimpleTableProviderComponents(all, "cat", com.dianping.dog.dal._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.dog.dal._INDEX.getDaoClasses());

      return all;
   }
}
