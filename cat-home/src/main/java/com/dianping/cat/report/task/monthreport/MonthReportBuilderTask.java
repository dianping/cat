package com.dianping.cat.report.task.monthreport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.monthreport.model.entity.MonthReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.home.dal.report.Monthreport;
import com.dianping.cat.home.dal.report.MonthreportDao;
import com.dianping.cat.home.dal.report.MonthreportEntity;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.DalNotFoundException;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class MonthReportBuilderTask implements Task {

	private static final long DAY = 24 * 60 * 60 * 1000L;

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private MonthreportDao m_monthreportDao;

	public MonthReport buildMonthReport(String domain, Date start, Date end) throws DalException, SAXException,
	      IOException {
		TransactionReport transactionReport = getTransactionReport(start, end, domain);
		EventReport eventReport = getEventReport(start, end, domain);
		ProblemReport problemReport = getProblemReport(start, end, domain);

		MonthReportBuilder builder = new MonthReportBuilder();
		MonthReport report = builder.build(transactionReport, eventReport, problemReport);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	private Set<String> getAllDomains(final Date start, final Date end) throws DalException {
		Set<String> domains = new HashSet<String>();
		List<Dailyreport> historyReports = m_dailyreportDao.findAllByPeriod(start, end,
		      DailyreportEntity.READSET_DOMAIN_NAME);

		for (Dailyreport report : historyReports) {
			domains.add(report.getDomain());
		}
		return domains;
	}

	private EventReport getEventReport(final Date start, final Date end, String domain) throws DalException,
	      SAXException, IOException {
		long startLong = Long.MAX_VALUE;
		long endLong = 0;
		long startTime = start.getTime();
		int days = (int) ((end.getTime() - startTime) / DAY);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		EventReportMerger merger = new EventReportMerger(new EventReport(domain));

		for (int i = 0; i < days; i++) {
			try {
				Dailyreport report = m_dailyreportDao.findByNameDomainPeriod(new Date(startTime + i * DAY), domain,
				      "event", DailyreportEntity.READSET_FULL);
				String xml = report.getContent();
				EventReport reportModel = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(xml);
				startLong = Math.min(startLong, reportModel.getStartTime().getTime());
				endLong = Math.max(startLong, reportModel.getEndTime().getTime());
				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				Cat.getProducer().logEvent("MonthReport", "event", "NotFound",
				      domain + sdf.format(new Date(startTime + i * DAY)));
			} catch (DalException e) {
				Cat.logError(e);
			}
		}
		EventReport eventReport = merger.getEventReport();

		eventReport.setStartTime(new Date(startLong));
		eventReport.setEndTime(new Date(endLong));
		return eventReport;
	}

	public Date getMonthFirstDay(int step) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, step);
		return cal.getTime();
	}

	private List<String> getMonthReportCreatedDomin(Date date) {
		List<String> domains = new ArrayList<String>();
		try {
			List<Monthreport> monthreports = m_monthreportDao.findByPeriod(date, MonthreportEntity.READSET_DOMAIN_PERIOD);
			for (Monthreport report : monthreports) {
				domains.add(report.getDomain());
			}
		} catch (DalNotFoundException e1) {
		} catch (DalException e) {
			Cat.logError(e);
		}

		return domains;
	}

	private ProblemReport getProblemReport(final Date start, final Date end, String domain) throws DalException,
	      SAXException, IOException {
		long startLong = Long.MAX_VALUE;
		long endLong = 0;
		long startTime = start.getTime();
		int days = (int) ((end.getTime() - startTime) / DAY);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));

		for (int i = 0; i < days; i++) {
			Dailyreport report = null;
			try {
				report = m_dailyreportDao.findByNameDomainPeriod(new Date(startTime + i * DAY), domain, "problem",
				      DailyreportEntity.READSET_FULL);
			} catch (DalException e) {
			}
			if (report != null) {
				String xml = report.getContent();

				ProblemReport reportModel = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(xml);
				startLong = Math.min(startLong, reportModel.getStartTime().getTime());
				endLong = Math.max(startLong, reportModel.getEndTime().getTime());
				reportModel.accept(merger);
			} else {
				Cat.getProducer().logEvent("MonthReport", "transaction", "NotFound",
				      domain + sdf.format(new Date(startTime + i * DAY)));
			}
		}

		ProblemReport problemReport = merger.getProblemReport();
		problemReport.setStartTime(new Date(startLong));
		problemReport.setEndTime(new Date(endLong));
		return problemReport;
	}

	private long getSleepTime() {
		long currentTime = System.currentTimeMillis();
		long nextDay = TaskHelper.tomorrowZero(new Date()).getTime() + 1000 * 60 * 10;
		return nextDay - currentTime;
	}

	private TransactionReport getTransactionReport(final Date start, final Date end, String domain) throws DalException,
	      SAXException, IOException {
		long startLong = Long.MAX_VALUE;
		long endLong = 0;
		long startTime = start.getTime();
		int days = (int) ((end.getTime() - startTime) / DAY);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));

		for (int i = 0; i < days; i++) {
			Dailyreport report = null;
			try {
				report = m_dailyreportDao.findByNameDomainPeriod(new Date(startTime + i * DAY), domain, "transaction",
				      DailyreportEntity.READSET_FULL);
			} catch (DalException e) {
			}
			if (report != null) {
				String xml = report.getContent();
				TransactionReport reportModel = DefaultSaxParser.parse(xml);
				startLong = Math.min(startLong, reportModel.getStartTime().getTime());
				endLong = Math.max(startLong, reportModel.getEndTime().getTime());
				reportModel.accept(merger);
			} else {
				Cat.getProducer().logEvent("MonthReport", "transaction", "NotFound",
				      domain + sdf.format(new Date(startTime + i * DAY)));
			}
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(new Date(startLong));
		transactionReport.setEndTime(new Date(endLong));
		return transactionReport;
	}

	public void run() {
		while (true) {
			try {
				Date lastMonth = getMonthFirstDay(-1);
				Date currentMonth = getMonthFirstDay(0);
				Set<String> allDomains = getAllDomains(lastMonth, currentMonth);
				List<String> createdDomains = getMonthReportCreatedDomin(lastMonth);

				for (String temp : createdDomains) {
					allDomains.remove(temp);
				}
				for (String domain : allDomains) {
					Transaction t = Cat.newTransaction("MonthReport", domain);
					t.setStatus(Transaction.SUCCESS);
					try {
						MonthReport report = buildMonthReport(domain, lastMonth, currentMonth);

						Monthreport entity = m_monthreportDao.createLocal();
						entity.setContent(report.toString());
						entity.setDomain(domain);
						entity.setName("monthreport");
						entity.setPeriod(lastMonth);

						m_monthreportDao.insert(entity);
					} catch (Exception e) {
						Cat.logError(e);
						t.setStatus(e);
					} finally {
						t.complete();
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				long sleepTime = getSleepTime();
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	@Override
	public String getName() {
		return "Month-Report-Builder";
	}

	@Override
	public void shutdown() {
	}
}
