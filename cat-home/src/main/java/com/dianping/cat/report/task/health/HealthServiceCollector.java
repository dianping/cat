package com.dianping.cat.report.task.health;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dainping.cat.consumer.core.dal.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.cross.DomainManager;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;

public class HealthServiceCollector {

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private DomainManager m_domainManager;

	private long m_timestamp;

	private Map<String, ServiceInfo> m_serviceInfos = new HashMap<String, ServiceInfo>();

	private void addCallinfo(TypeDetailInfo info) {
		String projectName = info.getProjectName();
		if (projectName.equals("UnknownProject") || projectName.equals("AllServers")) {
			return;
		}

		long totalCount = info.getTotalCount();
		double avgTime = info.getAvg();
		long failCount = info.getFailureCount();

		ServiceInfo call = m_serviceInfos.get(projectName);
		if (call == null) {
			call = new ServiceInfo();

			m_serviceInfos.put(projectName, call);
			call.setTotalCount(totalCount);
			call.setFailCount(failCount);
			call.setAvgTime(avgTime);
		} else {
			double sum = totalCount * avgTime + call.getTotalCount() * call.getAvgTime();
			long allTotalCount = call.getTotalCount() + totalCount;

			call.setTotalCount(allTotalCount);
			call.setFailCount(call.getFailCount() + failCount);
			call.setAvgTime(sum / allTotalCount);
		}
	}

	public synchronized void buildCrossInfo(long time) {
		if (m_timestamp != time) {
			m_timestamp = time;
			m_serviceInfos.clear();
			Set<String> domains = queryAllDomains(new Date(time));
			for (String domain : domains) {
				CrossReport report = queryCrossReport(new Date(time), domain);
				ProjectInfo projectInfo = new ProjectInfo(TimeUtil.ONE_HOUR);

				projectInfo.setDomainManager(m_domainManager);
				projectInfo.setClientIp(CatString.ALL);
				projectInfo.visitCrossReport(report);

				Collection<TypeDetailInfo> calls = projectInfo.getCallProjectsInfo();
				for (TypeDetailInfo info : calls) {
					addCallinfo(info);
				}
			}
		}
	}

	public Map<String, ServiceInfo> getServiceInfos() {
		return m_serviceInfos;
	}

	private Set<String> queryAllDomains(Date date) {
		List<Report> historyReports = null;
		try {
			historyReports = m_reportDao.findAllByDomainNameDuration(date, new Date(date.getTime() + TimeUtil.ONE_HOUR), null,
			      "cross", ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}

		Set<String> domainNames = new HashSet<String>();
		if (historyReports != null) {
			for (Report report : historyReports) {
				domainNames.add(report.getDomain());
			}
		}
		return domainNames;
	}

	private CrossReport queryCrossReport(Date date, String domain) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
		try {
			List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(date, domain, 1, "cross",
			      ReportEntity.READSET_FULL);

			for (Report report : reports) {
				String xml = report.getContent();
				CrossReport model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		CrossReport crossReport = merger.getCrossReport();

		return crossReport;
	}

	public static class ServiceInfo {
		private long m_totalCount;

		private long m_failCount;

		private double m_avgTime;

		public double getAvgTime() {
			return m_avgTime;
		}

		public long getFailCount() {
			return m_failCount;
		}

		public long getTotalCount() {
			return m_totalCount;
		}

		public void setAvgTime(double avgTime) {
			m_avgTime = avgTime;
		}

		public void setFailCount(long failCount) {
			m_failCount = failCount;
		}

		public void setTotalCount(long totalCount) {
			m_totalCount = totalCount;
		}
	}
}
