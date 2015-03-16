package com.dianping.cat.broker.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppConnectionDataDao;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.broker.api.app.service.AppService;
import com.dianping.cat.broker.api.app.service.impl.AppConnectionService;
import com.dianping.cat.broker.api.app.service.impl.AppDataService;
import com.dianping.cat.broker.api.app.service.impl.AppSpeedService;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;
import com.dianping.cat.build.AppDatabaseConfigurator;
import com.dianping.cat.config.app.AppCommandDataTableProvider;
import com.dianping.cat.config.app.AppConnectionTableProvider;
import com.dianping.cat.config.app.AppSpeedTableProvider;
import com.dianping.cat.config.url.UrlPatternConfigManager;
import com.dianping.cat.service.IpService;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(RequestUtils.class));
		all.add(C(MonitorManager.class).req(UrlPatternConfigManager.class, IpService.class));

		all.add(C(AppService.class, AppDataService.ID, AppDataService.class).req(AppCommandDataDao.class));
		all.add(C(AppService.class, AppConnectionService.ID, AppConnectionService.class).req(AppConnectionDataDao.class));
		all.add(C(AppService.class, AppSpeedService.ID, AppSpeedService.class).req(AppSpeedDataDao.class));

		all.add(C(TableProvider.class, "app-command-data", AppCommandDataTableProvider.class));
		all.add(C(TableProvider.class, "app-connection-data", AppConnectionTableProvider.class));
		all.add(C(TableProvider.class, "app-speed-data", AppSpeedTableProvider.class));

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		all.addAll(new AppDatabaseConfigurator().defineComponents());

		// Please keep it as last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}
}
