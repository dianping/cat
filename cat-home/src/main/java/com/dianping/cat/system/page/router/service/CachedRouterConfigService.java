package com.dianping.cat.system.page.router.service;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class CachedRouterConfigService implements Initializable {

	@Inject
	private RouterConfigService m_routerConfigService;

	private volatile RouterConfig m_routerConfig;

	@Override
	public void initialize() throws InitializationException {
		refresh();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return "router-refresh-task";
			}

			@Override
			public void handle() throws Exception {
				refresh();
			}
		});
	}

	public RouterConfig queryLastRouterConfig() {
		return m_routerConfig;
	}

	public void refresh() {
		m_routerConfig = m_routerConfigService.queryLastReport(Constants.CAT);
	}

}
