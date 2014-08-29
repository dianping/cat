package com.dianping.cat.report.task.monitor.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.home.OverloadReport.entity.OverloadReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class TableCapacityService extends ContainerHolder implements Initializable, LogEnabled {

	private Map<String, CapacityUpdater> m_updaters = new HashMap<String, CapacityUpdater>();

	private List<OverloadReport> m_overloadReports = new CopyOnWriteArrayList<OverloadReport>();

	private static final double CAPACITY = 5;

	private Logger m_logger;

	private boolean compareOverloadReport(OverloadReport originReport, OverloadReport compareRepoort, Date startTime,
	      Date endTime) {
		Date originDate = originReport.getPeriod();

		if (originDate.before(startTime) || originDate.after(endTime)) {
			return false;
		}

		String name = compareRepoort.getName();
		String domain = compareRepoort.getDomain();
		String ip = compareRepoort.getIp();

		if (StringUtils.isNotEmpty(name) && !name.equals(originReport.getName())) {
			return false;
		}
		if (StringUtils.isNotEmpty(domain) && !domain.equals(originReport.getDomain())) {
			return false;
		}
		if (StringUtils.isNotEmpty(ip) && !ip.equals(originReport.getIp())) {
			return false;
		}

		return true;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		m_updaters = lookupMap(CapacityUpdater.class);
		initializeOverloadReport();
		Threads.forGroup("cat").start(new CapacityUpdateTask());
	}

	private void initializeOverloadReport() {
		int loadDBBeginId = 0;

		for (CapacityUpdater updater : m_updaters.values()) {
			try {
				updater.updateOverloadReport(loadDBBeginId, m_overloadReports);

				String updaterName = updater.getId();
				Cat.logEvent("CapacityInitialize", updaterName, Event.SUCCESS, null);
				m_logger.info("CapacityInitialize success " + updaterName);
			} catch (DalException e) {
				Cat.logError(e);
			}
		}
	}

	public List<OverloadReport> queryOverloadReports(OverloadReport compareRepoort, Date startTime, Date endTime) {
		List<OverloadReport> reports = new ArrayList<OverloadReport>();

		for (OverloadReport report : m_overloadReports) {
			if (compareOverloadReport(report, compareRepoort, startTime, endTime)) {
				reports.add(report);
			}
		}

		Collections.sort(reports, new Comparator<OverloadReport>() {
			@Override
			public int compare(OverloadReport o1, OverloadReport o2) {
				long o1Mills = o1.getPeriod().getTime();
				long o2Mills = o2.getPeriod().getTime();

				if (o1Mills == o2Mills) {
					return 0;
				} else {
					return o1Mills > o2Mills ? -1 : 1;
				}
			}
		});

		return reports;
	}

	public class CapacityUpdateTask implements Task {

		private static final long DURATION = 60 * 60 * 1000;

		@Override
		public String getName() {
			return "capacity-update-task";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				String hourStr = String.valueOf(hour);

				if (hour < 10) {
					hourStr = "0" + hourStr;
				}

				Transaction t = Cat.newTransaction("UpdateCapacity", "H" + hourStr);
				long currentMills = System.currentTimeMillis();

				for (CapacityUpdater updater : m_updaters.values()) {
					try {
						int maxId = updater.updateDBCapacity(CAPACITY);

						if (maxId != -1) {
							updater.updateOverloadReport(maxId, m_overloadReports);
						}

						String updaterName = updater.getId();
						Cat.logEvent("CapacityUpdater:" + hourStr, updaterName, Event.SUCCESS, null);
						m_logger.info("CapacityUpdater success " + hourStr + " " + updaterName);
					} catch (DalException e) {
						Cat.logError(e);
					}
				}
				t.setStatus(Transaction.SUCCESS);
				t.complete();

				long currentDuration = System.currentTimeMillis() - currentMills;

				if (currentDuration < DURATION) {
					try {
						Thread.sleep(DURATION - currentDuration);
					} catch (InterruptedException e) {
						Cat.logError(e);
					}
				}

			}
		}

		@Override
		public void shutdown() {
		}

	}
}
