package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.browser.BrowserMetaAnalyzer;
import com.dianping.cat.consumer.browser.BrowserMetaReportMerger;
import com.dianping.cat.consumer.browsermeta.model.entity.BrowserMetaReport;
import com.dianping.cat.consumer.browsermeta.model.transform.DefaultNativeParser;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.AbstractReportService;

public class BrowserMetaReportService extends AbstractReportService<BrowserMetaReport> {

	@Override
	public BrowserMetaReport makeReport(String domain, Date start, Date end) {
		BrowserMetaReport report = new BrowserMetaReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	private BrowserMetaReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new BrowserMetaReport(domain);
		}
	}

	@Override
	public BrowserMetaReport queryDailyReport(String domain, Date start, Date end) {
		throw new RuntimeException("BrowserMetaReport report don't support daily report");
	}

	@Override
	public BrowserMetaReport queryHourlyReport(String domain, Date start, Date end) {
		BrowserMetaReportMerger merger = new BrowserMetaReportMerger(new BrowserMetaReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = BrowserMetaAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
				      HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					String xml = report.getContent();

					try {
						if (xml != null && xml.length() > 0) {// for old xml storage
							BrowserMetaReport reportModel = com.dianping.cat.consumer.browsermeta.model.transform.DefaultSaxParser
							      .parse(xml);
							reportModel.accept(merger);
						} else {// for new binary storage, binary is same to report id
							BrowserMetaReport reportModel = queryFromHourlyBinary(report.getId(), domain);

							reportModel.accept(merger);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		BrowserMetaReport metaReport = merger.getBrowserMetaReport();

		metaReport.setStartTime(start);
		metaReport.setEndTime(new Date(end.getTime() - 1));

		Set<String> domains = queryAllDomainNames(start, end, BrowserMetaAnalyzer.ID);
		metaReport.getDomainNames().addAll(domains);
		return metaReport;
	}

	@Override
	public BrowserMetaReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("BrowserMetaReport report don't support monthly report");
	}

	@Override
	public BrowserMetaReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("BrowserMetaReport report don't support weekly report");
	}

}
