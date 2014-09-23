package com.dianping.cat.report.task.alert.heartbeat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.heartbeat.DisplayHeartbeat;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertManager;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;
import com.dianping.cat.service.ProjectService;

public class HeartbeatAlert implements Task {

	@Inject
	private ProjectService m_projectService;

	@Inject
	private HostinfoService m_hostinfoService;

	@Inject(type = ModelService.class, value = HeartbeatAnalyzer.ID)
	private ModelService<HeartbeatReport> m_service;

	@Inject
	private GraphBuilder m_builder;

	@Inject
	protected AlertManager m_sendManager;

	protected static final long DURATION = TimeUtil.ONE_MINUTE;

	protected static final float gcAvgThreshold = 1;

	protected static final float systemLoadAvgThreshold = 1;

	private DisplayHeartbeat generateReport(String domain, String ip, long date) {
		ModelRequest request = new ModelRequest(domain, date) //
		      .setProperty("ip", ip);

		if (m_service.isEligable(request)) {
			ModelResponse<HeartbeatReport> response = m_service.invoke(request);
			HeartbeatReport report = response.getModel();

			if (report != null) {
				return new DisplayHeartbeat(m_builder).display(report, ip);
			}
		} else {
			throw new RuntimeException("Internal error: no eligable ip service registered for " + request + "!");
		}
		return null;
	}

	@Override
	public String getName() {
		return AlertType.HeartBeat.getName();
	}

	private void processDomain(String domain, int minute) {
		List<String> ips = m_hostinfoService.queryIpsByDomain(domain);

		for (String ip : ips) {
			processIp(domain, ip, minute);
		}
	}

	private void processIp(String domain, String ip, int minute) {
		try {
			double[] gcCounts = new double[10];
			double systemLoadCount = 0;

			if (minute < 9) {
				long currentMill = new Date().getTime();
				long currentHourMill = currentMill - currentMill % TimeUtil.ONE_HOUR;
				long lastHourMill = currentHourMill - TimeUtil.ONE_HOUR;
				DisplayHeartbeat lastHourReport = generateReport(domain, ip, lastHourMill);
				DisplayHeartbeat currentHourReport = generateReport(domain, ip, currentHourMill);

				if (lastHourReport != null) {
					int length = 9 - minute;
					int startIndex = 60 - length;

					System.arraycopy(lastHourReport.getOldGcCount(), startIndex, gcCounts, 0, length);
				}
				if (currentHourReport != null) {
					int copyLength = minute + 1;
					int desStartIndex = 10 - copyLength;

					System.arraycopy(currentHourReport.getOldGcCount(), 0, gcCounts, desStartIndex, copyLength);
					systemLoadCount = currentHourReport.getSystemLoadAverage()[minute];
				}
			} else {
				long currentMill = new Date().getTime();
				long currentHourMill = currentMill - currentMill % TimeUtil.ONE_HOUR;
				DisplayHeartbeat report = generateReport(domain, ip, currentHourMill);

				if (report != null) {
					int srcStartIndex = minute - 9;

					System.arraycopy(report.getOldGcCount(), srcStartIndex, gcCounts, 0, 10);
					systemLoadCount = report.getSystemLoadAverage()[minute];
				}
			}

			if (systemLoadCount > systemLoadAvgThreshold) {
				AlertEntity entity = new AlertEntity();

				entity.setDate(new Date()).setContent("system load avg error").setLevel("error");
				entity.setMetric(domain + " " + ip + "system load avg").setType(getName()).setGroup(domain);

				m_sendManager.addAlert(entity);
			}
			
		} catch (Exception ex) {
			Cat.logError(ex);
		}
	}

	@Override
	public void run() {
		boolean active = true;

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}
		while (active) {
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("HeartbeatAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			try {
				Set<String> domains = m_projectService.findAllDomain();

				for (String domain : domains) {
					processDomain(domain, minute);
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			} finally {
				t.complete();
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
