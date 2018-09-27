package com.dianping.cat.report.task.reload;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.helper.TimeHelper;

@Named
public class ReportReloadTask extends ContainerHolder implements Initializable, Task {

	private static final long DURATION = TimeHelper.ONE_HOUR;

	@Inject
	private ReportReloadConfigManager m_configManager;

	private Map<String, ReportReloader> m_reloaders;

	@Override
	public String getName() {
		return "report-reload-task";
	}

	@Override
	public void initialize() throws InitializationException {
		m_reloaders = lookupMap(ReportReloader.class);
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();

			try {
				for (Entry<String, ReportReloader> entry : m_reloaders.entrySet()) {
					String type = entry.getKey();
					List<Date> dates = m_configManager.queryByReportType(type);

					for (Date date : dates) {
						ReportReloader reloader = entry.getValue();

						reloader.reload(date.getTime());
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {

	}
}
