package com.dianping.dog.home.build;

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

      defineSimpleTableProviderComponents(all, "cat", com.dianping.dog.home.dal.alarm._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.dog.home.dal.alarm._INDEX.getDaoClasses());

      defineSimpleTableProviderComponents(all, "cat", com.dianping.dog.home.dal.notification._INDEX.getEntityClasses());
      defineDaoComponents(all, com.dianping.dog.home.dal.notification._INDEX.getDaoClasses());

      return all;
   }
}
