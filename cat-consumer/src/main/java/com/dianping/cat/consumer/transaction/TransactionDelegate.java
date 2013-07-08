package com.dianping.cat.consumer.transaction;

import static com.dianping.cat.service.ReportConstants.ALL;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.core.dal.TaskDao;
import com.dianping.cat.service.ReportConstants;
import com.dianping.cat.service.ReportDelegate;

public class TransactionDelegate implements ReportDelegate<TransactionReport> {

	@Inject
	private TaskDao m_taskDao;

	@Override
	public void afterLoad(Map<String, TransactionReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, TransactionReport> reports) {
		for (TransactionReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}

		TransactionReport all = createAggregatedTypeReport(reports);

		reports.put(all.getDomain(), all);
	}

	@Override
	public String buildXml(TransactionReport report) {
		report.accept(new TransactionStatisticsComputer());

		String xml = new TransactionReportUrlFilter().buildXml(report);

		return xml;
	}

	private TransactionReport createAggregatedTypeReport(Map<String, TransactionReport> reports) {
		TransactionReport first = reports.values().iterator().next();
		TransactionReport all = makeReport(ALL, first.getStartTime().getTime(), ReportConstants.HOUR);
		TransactionReportTypeAggregator visitor = new TransactionReportTypeAggregator(all);

		try {
			for (TransactionReport report : reports.values()) {
				String domain = report.getDomain();

				all.getIps().add(domain);
				all.getDomainNames().add(domain);

				visitor.visitTransactionReport(report);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return all;
	}

	@Override
	public String getDomain(TransactionReport report) {
		return report.getDomain();
	}

	@Override
	public TransactionReport makeReport(String domain, long startTime, long duration) {
		TransactionReport report = new TransactionReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public TransactionReport mergeReport(TransactionReport old, TransactionReport other) {
		TransactionReportMerger merger = new TransactionReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public TransactionReport parseXml(String xml) throws Exception {
		TransactionReport report = DefaultSaxParser.parse(xml);

		return report;
	}

	@Override
	public boolean createHourlyTask(TransactionReport report) {
		try {
			Task task = m_taskDao.createLocal();
			task.setCreationDate(new Date());
			task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			task.setReportDomain(report.getDomain());
			task.setReportName("transaction");
			task.setReportPeriod(report.getStartTime());
			task.setStatus(1);

			m_taskDao.insert(task);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
	}
}
