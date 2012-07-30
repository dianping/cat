package com.dianping.cat.report.task;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.GraphDao;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public abstract class AbstractReportBuilder {

	@Inject
	protected ReportDao m_reportDao;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected DailyreportDao m_dailyReportDao;

	protected void getDomainSet(Set<String> domainSet, Date start, Date end) {
		List<Report> domainNames = new ArrayList<Report>();
		try {
			domainNames = m_reportDao
			      .findAllByDomainNameDuration(start, end, null, null, ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}

		if (domainNames == null || domainNames.size() == 0) {
			return; // no hourly report
		}

		for (Report domainName : domainNames) {
			domainSet.add(domainName.getDomain());
		}
	}

	// clear graphs from databases
	protected void clearHourlyGraphs(List<Graph> graphs) throws DalException {
		for (Graph graph : graphs) {
			this.m_graphDao.deleteByDomainNamePeriodIp(graph);
		}
	}

	// clear daily graph from databases
	protected void clearDailyReport(Dailyreport report) throws DalException {
		this.m_dailyReportDao.deleteByDomainNamePeriod(report);
	}

	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void writeReportToFile(String domain, String name, Date period, String content) {
		String fileName = (domain + "_" + name + "_" + df.format(period)).replace(' ', '_').replace(':', '_');
		String inFile = "D:\\cat_report\\" + fileName;
		try {
			FileWriter fw = new FileWriter(inFile);
			fw.write(content);
			if (fw != null)
				fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
