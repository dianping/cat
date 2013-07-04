package com.dianping.cat.report.task.dependency;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class DependencyReportBuilder implements ReportBuilder {

	@Inject
	private ReportService m_reportService;

	@Inject
	private TopologyGraphBuilder m_graphBuilder;

	@Inject
	private TopologyGraphDao m_topologyGraphDao;

	@Override
	public boolean buildDailyReport(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no daily report builder for dependency!");
	}

	@Override
	public boolean buildHourReport(String name, String reportDomain, Date reportPeriod) {
		Date end = new Date(reportPeriod.getTime() + TimeUtil.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(reportPeriod, end, "dependency");
		boolean result = true;

		m_graphBuilder.getGraphs().clear();
		for (String domain : domains) {
			DependencyReport report = m_reportService.queryDependencyReport(domain, reportPeriod, end);

			m_graphBuilder.visitDependencyReport(report);
		}

		Map<Long, TopologyGraph> graphs = m_graphBuilder.getGraphs();
		for (Entry<Long, TopologyGraph> entry : graphs.entrySet()) {
			try {
				Date date = new Date(entry.getKey());
				TopologyGraph graph = entry.getValue();

				com.dianping.cat.home.dal.report.TopologyGraph proto = m_topologyGraphDao.createLocal();
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				proto.setType(3);
				proto.setPeriod(date);
				proto.setCreationDate(new Date());
				proto.setIp(ip);
				proto.setContent(DefaultNativeBuilder.build(graph));

				m_topologyGraphDao.insert(proto);
			} catch (Exception e) {
				result = false;
				Cat.logError(e);
			}
		}
		return result;
	}

	@Override
	public boolean buildMonthReport(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no month report builder for dependency!");
	}

	@Override
	public boolean buildWeeklyReport(String name, String reportDomain, Date reportPeriod) {
		throw new UnsupportedOperationException("no week report builder for dependency!");
	}

}
