package com.dianping.cat.report.analyzer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ReportConstants;

@RunWith(JUnit4.class)
public class ArchYearAnalyzer extends ComponentTestCase {

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy/MM/dd");

	@Inject
	private ReportService m_reportService;


	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_reportService = lookup(ReportService.class);
	}

	private Set<String> getDomains(Date start) {
		return m_reportService.queryAllDomainNames(start, new Date(start.getTime() + TimeUtil.ONE_DAY), TransactionAnalyzer.ID);
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
			Machine machine = report.findOrCreateMachine(ReportConstants.ALL);
			Collection<TransactionType> types = machine.getTypes().values();
			for (TransactionType type : types) {
				String name = type.getId();
				long count = type.getTotalCount();
				long error = type.getFailCount();
				double sum = type.getSum();

				if (name.equalsIgnoreCase("call") || name.equalsIgnoreCase("pigeonCall")) {
					m_call.add(count, error, sum);
				}
				if (name.equalsIgnoreCase(ReportConstants.REPORT_SERVICE) || name.equalsIgnoreCase("pigeonService")) {
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

		@Override
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

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append(m_count).append("\t").append(getAvg()).append("\t").append(m_error).append("\t");
			return sb.toString();
		}
	}
}
