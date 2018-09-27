package com.dianping.cat.report.task.reload.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.task.reload.AbstractReportReloader;
import com.dianping.cat.report.task.reload.ReportReloadEntity;
import com.dianping.cat.report.task.reload.ReportReloader;

@Named(type = ReportReloader.class, value = HeartbeatAnalyzer.ID)
public class HeartbeatReportReloader extends AbstractReportReloader {

	@Inject(HeartbeatAnalyzer.ID)
	protected ReportManager<HeartbeatReport> m_reportManager;

	private List<HeartbeatReport> buildMergedReports(Map<String, List<HeartbeatReport>> mergedReports) {
		List<HeartbeatReport> results = new ArrayList<HeartbeatReport>();

		for (Entry<String, List<HeartbeatReport>> entry : mergedReports.entrySet()) {
			String domain = entry.getKey();
			HeartbeatReport report = new HeartbeatReport(domain);
			HeartbeatReportMerger merger = new HeartbeatReportMerger(report);

			report.setStartTime(report.getStartTime());
			report.setEndTime(report.getEndTime());

			for (HeartbeatReport r : entry.getValue()) {
				r.accept(merger);
			}
			results.add(merger.getHeartbeatReport());
		}

		return results;
	}

	@Override
	public String getId() {
		return HeartbeatAnalyzer.ID;
	}

	@Override
	public List<ReportReloadEntity> loadReport(long time) {
		List<ReportReloadEntity> results = new ArrayList<ReportReloadEntity>();
		Map<String, List<HeartbeatReport>> mergedReports = new HashMap<String, List<HeartbeatReport>>();

		for (int i = 0; i < getAnalyzerCount(); i++) {
			Map<String, HeartbeatReport> reports = m_reportManager.loadLocalReports(time, i);

			for (Entry<String, HeartbeatReport> entry : reports.entrySet()) {
				String domain = entry.getKey();
				HeartbeatReport r = entry.getValue();
				List<HeartbeatReport> rs = mergedReports.get(domain);

				if (rs == null) {
					rs = new ArrayList<HeartbeatReport>();

					mergedReports.put(domain, rs);
				}
				rs.add(r);
			}
		}

		List<HeartbeatReport> reports = buildMergedReports(mergedReports);

		for (HeartbeatReport r : reports) {
			HourlyReport report = new HourlyReport();

			report.setCreationDate(new Date());
			report.setDomain(r.getDomain());
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(getId());
			report.setPeriod(new Date(time));
			report.setType(1);

			byte[] content = DefaultNativeBuilder.build(r);
			ReportReloadEntity entity = new ReportReloadEntity(report, content);

			results.add(entity);
		}
		return results;
	}
}
