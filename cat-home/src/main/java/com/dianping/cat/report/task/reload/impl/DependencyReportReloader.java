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
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.task.reload.AbstractReportReloader;
import com.dianping.cat.report.task.reload.ReportReloadEntity;
import com.dianping.cat.report.task.reload.ReportReloader;

@Named(type = ReportReloader.class, value = DependencyAnalyzer.ID)
public class DependencyReportReloader extends AbstractReportReloader {

	@Inject(DependencyAnalyzer.ID)
	protected ReportManager<DependencyReport> m_reportManager;

	private List<DependencyReport> buildMergedReports(Map<String, List<DependencyReport>> mergedReports) {
		List<DependencyReport> results = new ArrayList<DependencyReport>();

		for (Entry<String, List<DependencyReport>> entry : mergedReports.entrySet()) {
			String domain = entry.getKey();
			DependencyReport report = new DependencyReport(domain);
			DependencyReportMerger merger = new DependencyReportMerger(report);

			report.setStartTime(report.getStartTime());
			report.setEndTime(report.getEndTime());

			for (DependencyReport r : entry.getValue()) {
				r.accept(merger);
			}
			results.add(merger.getDependencyReport());
		}

		return results;
	}

	@Override
	public String getId() {
		return DependencyAnalyzer.ID;
	}

	@Override
	public List<ReportReloadEntity> loadReport(long time) {
		List<ReportReloadEntity> results = new ArrayList<ReportReloadEntity>();
		Map<String, List<DependencyReport>> mergedReports = new HashMap<String, List<DependencyReport>>();

		for (int i = 0; i < getAnalyzerCount(); i++) {
			Map<String, DependencyReport> reports = m_reportManager.loadLocalReports(time, i);

			for (Entry<String, DependencyReport> entry : reports.entrySet()) {
				String domain = entry.getKey();
				DependencyReport r = entry.getValue();
				List<DependencyReport> rs = mergedReports.get(domain);

				if (rs == null) {
					rs = new ArrayList<DependencyReport>();

					mergedReports.put(domain, rs);
				}
				rs.add(r);
			}
		}

		List<DependencyReport> reports = buildMergedReports(mergedReports);

		for (DependencyReport r : reports) {
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
