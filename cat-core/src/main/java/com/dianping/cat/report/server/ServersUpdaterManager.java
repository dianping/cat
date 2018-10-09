package com.dianping.cat.report.server;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class ServersUpdaterManager implements Initializable {

	@Inject
	private ServersUpdater m_remoteServerUpdater;

	@Inject
	private RemoteServersManager m_remoteServersManager;

	@Override
	public void initialize() throws InitializationException {
		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return "remote-server-updater";
			}

			@Override
			public void handle() throws Exception {
				try {
					long currentHour = TimeHelper.getCurrentHour().getTime();
					Map<String, Set<String>> currentServers = m_remoteServerUpdater.buildServers(new Date(currentHour));

					m_remoteServersManager.setCurrentServers(currentServers);

					long lastHour = currentHour - TimeHelper.ONE_HOUR;
					Map<String, Set<String>> lastServers = m_remoteServerUpdater.buildServers(new Date(lastHour));

					m_remoteServersManager.setLastServers(lastServers);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		});
	}
}
