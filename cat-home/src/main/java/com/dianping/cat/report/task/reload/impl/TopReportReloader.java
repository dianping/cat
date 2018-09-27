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
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopReportMerger;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.task.reload.AbstractReportReloader;
import com.dianping.cat.report.task.reload.ReportReloadEntity;
import com.dianping.cat.report.task.reload.ReportReloader;

@Named(type = ReportReloader.class, value = TopAnalyzer.ID)
public class TopReportReloader extends AbstractReportReloader {

	@Inject(TopAnalyzer.ID)
	protected ReportManager<TopReport> m_reportManager;

	private List<TopReport> buildMergedReports(Map<String, List<TopReport>> mergedReports) {
		List<TopReport> results = new ArrayList<TopReport>();

		for (Entry<String, List<TopReport>> entry : mergedReports.entrySet()) {
			String domain = entry.getKey();
			TopReport report = new TopReport(domain);
			TopReportMerger merger = new TopReportMerger(report);

			report.setStartTime(report.getStartTime());
			report.setEndTime(report.getEndTime());

			for (TopReport r : entry.getValue()) {
				r.accept(merger);
			}
			results.add(merger.getTopReport());
		}

		return results;
	}

	@Override
	public String getId() {
		return TopAnalyzer.ID;
	}

	@Override
	public List<ReportReloadEntity> loadReport(long time) {
		List<ReportReloadEntity> results = new ArrayList<ReportReloadEntity>();
		Map<String, List<TopReport>> mergedReports = new HashMap<String, List<TopReport>>();

		for (int i = 0; i < getAnalyzerCount(); i++) {
			Map<String, TopReport> reports = m_reportManager.loadLocalReports(time, i);

			for (Entry<String, TopReport> entry : reports.entrySet()) {
				String domain = entry.getKey();
				TopReport r = entry.getValue();
				List<TopReport> rs = mergedReports.get(domain);

				if (rs == null) {
					rs = new ArrayList<TopReport>();

					mergedReports.put(domain, rs);
				}
				rs.add(r);
			}
		}

		List<TopReport> reports = buildMergedReports(mergedReports);

		for (TopReport r : reports) {
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
