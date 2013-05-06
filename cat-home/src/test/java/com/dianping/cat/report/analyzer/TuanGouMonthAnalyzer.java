package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.page.cross.DomainManager;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;
import com.dianping.cat.report.task.health.HealthReportMerger;

@RunWith(JUnit4.class)
public class TuanGouMonthAnalyzer extends ComponentTestCase {

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private DomainManager m_domainManager;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_dailyreportDao = lookup(DailyreportDao.class);
		m_domainManager = lookup(DomainManager.class);
	}

	@Test
	public void test() {
		try {
			buildData();
		} catch (Exception e) {
			System.out.println("BuildError" + e);
		}
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

	private void buildOtherDomainCrossInfo(String domain, Date start, Date end, List<String> domains) {
		for (long current = start.getTime(); current < end.getTime(); current += TimeUtil.ONE_DAY) {
			Date startDate = new Date(current);
			Date endDate = new Date(current + TimeUtil.ONE_DAY);

			CrossReport crossReport = queryCrossReport(startDate, endDate, domain);
			
			System.out.print(sdf.format(startDate) + "\t");
			for (String otherDomain : domains) {
				Indicator otherDomainIndicator = getOtherDomainCrossInfo(otherDomain, crossReport);
				System.out.print(otherDomainIndicator);
			}
			System.out.println();
		}
	}

	private void buildServiceDetailInfo(String domain, Date start, Date end, List<String> functionNames) {
		for (long current = start.getTime(); current < end.getTime(); current += TimeUtil.ONE_DAY) {
			Date startDate = new Date(current);
			Date endDate = new Date(current + TimeUtil.ONE_DAY);
			TransactionReport transactionReport = queryTransactionReport(startDate, endDate, domain);
			System.out.print(sdf.format(startDate) + "\t");
			for (String functionName : functionNames) {
				Indicator functionIndicator = getFunctionDetailIndicator(functionName, transactionReport);
				System.out.print(functionIndicator);
			}
			System.out.println();
		}
	}

	private void buildServiceTotalInfo(String domain, Date start, Date end) {
		System.out.println(domain + " Service Info >>>>>>>>");
		for (long current = start.getTime(); current < end.getTime(); current += TimeUtil.ONE_DAY) {
			Date startDate = new Date(current);
			Date endDate = new Date(current + TimeUtil.ONE_DAY);
			TransactionReport transactionReport = queryTransactionReport(startDate, endDate, domain);
			HealthReport healthReport = queryHealthReport(startDate, endDate, domain);

			Indicator indicator = getServiceTotalIndicator(transactionReport, healthReport);
			System.out.println(sdf.format(startDate) + '\t' + indicator);
		}
	}

	private void buildUrlDetailsInfo(String domain, Date start, Date end, List<String> urls) {
		for (long current = start.getTime(); current < end.getTime(); current += TimeUtil.ONE_DAY) {
			Date startDate = new Date(current);
			Date endDate = new Date(current + TimeUtil.ONE_DAY);
			TransactionReport transactionReport = queryTransactionReport(startDate, endDate, domain);
			System.out.print(sdf.format(startDate) + "\t");
			for (String url : urls) {
				Indicator functionIndicator = getUrlDetailIndicator(transactionReport, url);
				System.out.print(functionIndicator);
			}
			System.out.println();
		}
	}

	private void buildUrlTotalInfo(String domain, Date start, Date end) {
		System.out.println(domain + " Url Info");
		for (long current = start.getTime(); current < end.getTime(); current += TimeUtil.ONE_DAY) {
			Date startDate = new Date(current);
			Date endDate = new Date(current + TimeUtil.ONE_DAY);
			TransactionReport transactionReport = queryTransactionReport(startDate, endDate, domain);

			Indicator indicator = getUrlTotalIndicator(transactionReport);
			System.out.println(sdf.format(startDate) + '\t' + indicator);
		}
	}

	public Date calMonthFirstDay(int step) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, step);
		return cal.getTime();
	}

	private Indicator getFunctionDetailIndicator(String functionName, TransactionReport report) {
		TransactionType type = getPigeonServiceTransaction(report);
		Indicator indicator = new Indicator();

		if (type != null) {
			TransactionName name = type.findName(functionName);
			if (name == null) {
				int index = functionName.indexOf("(");
				if (index > 0) {
					functionName = functionName.substring(0, index);
					name = type.findName(functionName);
				}
			}
			if (name != null) {
				indicator.setTotalCount(name.getTotalCount());
				indicator.set95Line(name.getLine95Value());
				indicator.setAvg(name.getAvg());
				indicator.setFailureCount(name.getFailCount());
			}
		}
		return indicator;
	}

	private Indicator getOtherDomainCrossInfo(String otherDomain, CrossReport report) {
		ProjectInfo projectInfo = new ProjectInfo(TimeUtil.ONE_HOUR);
		projectInfo.setDomainManager(m_domainManager);
		projectInfo.setClientIp("All");
		projectInfo.visitCrossReport(report);

		List<TypeDetailInfo> types = projectInfo.getServiceProjectsInfo();

		Indicator indicator = new Indicator();
		for (TypeDetailInfo info : types) {
			if (info.getProjectName().equals(otherDomain)) {
				indicator.setAvg(info.getAvg());
				indicator.setTotalCount(info.getTotalCount());
				indicator.setFailureCount(info.getFailureCount());
				break;
			}
		}
		return indicator;
	}

	private TransactionType getPigeonServiceTransaction(TransactionReport report) {
		TransactionType type = report.findOrCreateMachine("All").getTypes().get("Service");
		if (type == null) {
			type = report.getMachines().get("All").getTypes().get("PigeonService");
		}
		String pigeonName = "piegonService:heartTaskService:heartBeat()";

		if (type != null) {
			TransactionName name = type.findName(pigeonName);
			if (name == null) {
				name = type.findName("piegonService:heartTaskService:heartBeat");
			}
			if (name != null) {
				type.setTotalCount(type.getTotalCount() - name.getTotalCount());
				double sum = type.getSum() - name.getSum();
				type.setAvg(sum / type.getTotalCount());
			}
		}
		return type;
	}

	private Indicator getServiceTotalIndicator(TransactionReport transactionReport, HealthReport healthReport) {
		TransactionType type = getPigeonServiceTransaction(transactionReport);

		Indicator indicator = new Indicator();
		if (type != null) {
			indicator.set95Line(type.getLine95Value());
			indicator.setAvg(type.getAvg());
			indicator.setTotalCount(type.getTotalCount());
		}

		try {
			indicator.setFailureCount(healthReport.getClientService().getBaseInfo().getErrorTotal());
		} catch (Exception e) {
		}
		return indicator;
	}

	private Indicator getUrlDetailIndicator(TransactionReport transactionReport, String detailUrl) {
		TransactionType type = transactionReport.findOrCreateMachine("All").findType("URL");

		Indicator indicator = new Indicator();
		if (type != null) {
			TransactionName name = type.findName(detailUrl);
			if (name != null) {

				indicator.set95Line(name.getLine95Value());
				indicator.setAvg(name.getAvg());
				indicator.setTotalCount(name.getTotalCount());
				indicator.setFailureCount(name.getFailCount());
			}
		}
		return indicator;
	}

	private Indicator getUrlTotalIndicator(TransactionReport transactionReport) {
		TransactionType type = transactionReport.findOrCreateMachine("All").findType("URL");

		Indicator indicator = new Indicator();
		if (type != null) {
			indicator.set95Line(type.getLine95Value());
			indicator.setAvg(type.getAvg());
			indicator.setTotalCount(type.getTotalCount());
			indicator.setFailureCount(type.getFailCount());
		}
		return indicator;
	}

	public CrossReport queryCrossReport(Date start, Date end, String domain) {
		CrossReport crossReport = null;
		try {
			List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(start, end, domain, "cross",
			      DailyreportEntity.READSET_FULL);
			CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));
			for (Dailyreport report : reports) {
				String xml = report.getContent();
				CrossReport reportModel = com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			}
			crossReport = merger.getCrossReport();
		} catch (Exception e) {
			Cat.logError(e);
		}
		return crossReport;
	}

	private HealthReport queryHealthReport(Date startDate, Date endDate, String domain) {
		try {
			List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(startDate, endDate, domain, "health",
			      DailyreportEntity.READSET_FULL);
			HealthReportMerger merger = new HealthReportMerger(new HealthReport(domain));
			HealthReport healthReport = merger.getHealthReport();
			merger.setDuration(endDate.getTime() - startDate.getTime());

			for (Dailyreport report : reports) {
				String xml = report.getContent();
				HealthReport model = DefaultSaxParser.parse(xml);
				model.accept(merger);
				healthReport.getDomainNames().addAll(model.getDomainNames());
			}
			return healthReport;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HealthReport(domain);
	}

	private TransactionReport queryTransactionReport(Date startDate, Date endDate, String domain) {
		TransactionReport transactionReport = null;
		try {
			List<Dailyreport> reports = m_dailyreportDao.findAllByDomainNameDuration(startDate, endDate, domain,
			      "transaction", DailyreportEntity.READSET_FULL);
			TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));
			for (Dailyreport report : reports) {
				String xml = report.getContent();
				TransactionReport reportModel = com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser
				      .parse(xml);
				reportModel.accept(merger);
			}
			transactionReport = merger.getTransactionReport();
		} catch (Exception e) {
			Cat.logError(e);
		}
		return transactionReport;
	}

	@Test
	public void build() throws Exception{
		String domain = "TuanGouApiMobile";
		Date lastMonth = calMonthFirstDay(-2);
		Date nextMonth = calMonthFirstDay(-1);
		
		nextMonth = new Date(nextMonth.getTime()-TimeUtil.ONE_DAY*5);
		
		TransactionReportMerger meger = new TransactionReportMerger(new TransactionReport(domain));
		
		for (long current = lastMonth.getTime(); current < nextMonth.getTime(); current += TimeUtil.ONE_DAY) {
			Date startDate = new Date(current);
			System.out.println(startDate);
			Date endDate = new Date(current + TimeUtil.ONE_DAY);
			TransactionReport transactionReport = queryTransactionReport(startDate, endDate, domain);
			
			transactionReport.accept(meger);
		}

		TransactionReport transactionReport = meger.getTransactionReport();
		System.out.println(transactionReport);
		
		Machine machine = transactionReport.findMachine(CatString.ALL);
		TransactionType type = machine.getTypes().get("URL");
		
		for(TransactionName name :type.getNames().values()){
			System.out.println(name.getId()+"\t"+name.getTotalCount()+"\t"+name.getFailCount()+"\t"+name.getAvg());
		}
	}
	
	public void buildData() throws Exception {
		Date lastMonth = calMonthFirstDay(0);
		Date nextMonth = calMonthFirstDay(1);
		List<String> otherDomains = new ArrayList<String>();
		List<String> urls = new ArrayList<String>();
		String domain = "TuanGouRemote";

		otherDomains.add("TuanMobileApi");
		otherDomains.add("TuanGouWeb");
		otherDomains.add("TuanGouApiMobile");
		otherDomains.add("ShopWeb");

		List<String> functionNames = new ArrayList<String>();
		functionNames.add("dealRemoteService:dealRemoteService_1.0.0:getDeal(Integer)");
		functionNames.add("dealGroupRemoteService:dealGroupRemoteService_1.0.0:getActiveDealGroupList(DealListRequest)");
		functionNames.add("receiptRemoteService:receiptRemoteService_1.0.0:getReceiptList(Integer,Integer,Boolean)");

		try {
			Thread.sleep(1000 * 2);
		} catch (InterruptedException e) {
		}
		buildServiceTotalInfo(domain, lastMonth, nextMonth);
		buildServiceDetailInfo(domain, lastMonth, nextMonth, functionNames);
		buildOtherDomainCrossInfo(domain, lastMonth, nextMonth, otherDomains);

		domain = "TuanGouWeb";
		urls.add("/index");
		urls.add("/detail");
		urls.add("/ajax/getaids");
		buildUrlTotalInfo(domain, lastMonth, nextMonth);
		buildUrlDetailsInfo(domain, lastMonth, nextMonth, urls);

		domain = "BCTuangouWeb";
		buildUrlTotalInfo(domain, lastMonth, nextMonth);
		domain = "TuanGouApi";
		buildUrlTotalInfo(domain, lastMonth, nextMonth);
		domain = "TuanGouApiMobile";
		buildUrlTotalInfo(domain, lastMonth, nextMonth);
		
	}

	public static class Indicator {
		private long m_totalCount;

		private long m_failureCount;

		private double m_avg;

		private double m_95Line;

		public double get95Line() {
			return m_95Line;
		}

		public double getAvg() {
			return m_avg;
		}

		public long getFailureCount() {
			return m_failureCount;
		}

		public long getTotalCount() {
			return m_totalCount;
		}

		public void set95Line(double line) {
			m_95Line = line;
		}

		public void setAvg(double avg) {
			m_avg = avg;
		}

		public void setFailureCount(long failureCount) {
			m_failureCount = failureCount;
		}

		public void setTotalCount(long totalCount) {
			m_totalCount = totalCount;
		}

		@Override
		public String toString() {
			return new StringBuilder().append(m_totalCount).append('\t').append(m_failureCount).append('\t').append(m_avg)
			      .append('\t').append(m_95Line).append('\t').toString();
		}
	}
}
