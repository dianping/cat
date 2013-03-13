package com.dianping.cat.report.analyzer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.service.ReportService;

@RunWith(JUnit4.class)
public class ArchYearAnalyzer extends ComponentTestCase {

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy/MM/dd");

	@Inject
	private ReportService m_reportService;

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		m_reportService = lookup(ReportService.class);
		m_dailyreportDao = lookup(DailyreportDao.class);
	}

	private Set<String> getDomains(Date start) {
		Set<String> domains = new HashSet<String>();

		try {
			List<Dailyreport> reports = m_dailyreportDao.findAllDomainsByNameDuration(start, new Date(start.getTime()
			      + TimeUtil.ONE_DAY), "transaction", DailyreportEntity.READSET_DOMAIN_NAME);

			for (Dailyreport report : reports) {
				domains.add(report.getDomain());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return domains;
	}

	@Test
	public void builderData() throws IOException, ParseException {
		String startDate = "2012/11/18";

		Date start = m_sdf.parse(startDate);
		for (long i = start.getTime(); i < System.currentTimeMillis(); i = i + TimeUtil.ONE_DAY) {
			try {
	         Indicator state = processOneDay(new Date(i));

	         System.out.println(state);
         } catch (Exception e) {
	         e.printStackTrace();
         }
		}
	}

	private Indicator processOneDay(Date date) {
		Indicator indicator = new Indicator(m_sdf.format(date));
		Set<String> domains = getDomains(date);
		for (String domain : domains) {
			TransactionReport report = m_reportService.queryTransactionReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_DAY));

			if (report != null) {
				indicator.accept(report);
			}
		}

		return indicator;
	}

	public static class Indicator {
		private String m_date;

		private Item m_call = new Item();

		private Set<String> m_domains = new HashSet<String>();

		private Set<String> m_ips = new HashSet<String>();

		public Indicator(String date) {
			m_date = date;
		}

		public void accept(TransactionReport report) {
			Machine machine = report.findOrCreateMachine(CatString.ALL_IP);
			Collection<TransactionType> types = machine.getTypes().values();
			for (TransactionType type : types) {
				String name = type.getId();
				long count = type.getTotalCount();
				long error = type.getFailCount();
				double sum = type.getSum();

				if (name.equalsIgnoreCase("call") || name.equalsIgnoreCase("pigeonCall")) {
					m_call.add(count, error, sum);
				}
				if (name.equalsIgnoreCase("service") || name.equalsIgnoreCase("pigeonService")) {
					m_domains.add(report.getDomain());
					m_ips.addAll(report.getIps());
				}
			}
		}

		public Item getCall() {
			return m_call;
		}

		public void setCall(Item call) {
			m_call = call;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append(m_date + "\t").append(m_call).append(m_domains.size()).append('\t').append(m_ips.size());
			return sb.toString();
		}
	}

	public static class Item {

		private double m_avg;

		private long m_count;

		private long m_error;

		private double m_sum;

		public void add(long count, long error, double sum) {
			m_count += count;
			m_error += error;
			m_sum += sum;
		}

		public double getAvg() {
			if (m_count > 0) {
				return (double) m_sum / m_count;
			}
			return m_avg;
		}

		public long getCount() {
			return m_count;
		}

		public long getError() {
			return m_error;
		}

		public double getSum() {
			return m_sum;
		}

		public void setAvg(double avg) {
			m_avg = avg;
		}

		public void setCount(long count) {
			m_count = count;
		}

		public void setError(long error) {
			m_error = error;
		}

		public void setSum(double sum) {
			m_sum = sum;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append(m_count).append("\t").append(getAvg()).append("\t").append(m_error).append("\t");
			return sb.toString();
		}
	}
}
