package com.dianping.cat.report.analyzer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;

@RunWith(JUnit4.class)
public class ArchTransactionAnalyzer extends ComponentTestCase {

	@Inject
	private DailyreportDao m_dailyreportDao;

	private Map<String, DomainInfo> m_infos = new LinkedHashMap<String, DomainInfo>();

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_dailyreportDao = lookup(DailyreportDao.class);
	}

	private Set<String> queryAllDomain(Date start, Date end) {
		Set<String> domains = new HashSet<String>();

		try {
			List<Dailyreport> reports = m_dailyreportDao.findAllDomainsByNameDuration(start, end, "transaction",
			      DailyreportEntity.READSET_DOMAIN_NAME);

			for (Dailyreport report : reports) {
				domains.add(report.getDomain());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return domains;
	}

	@Test
	public void test() {
		Date start = TimeUtil.getLastMonth();
		Date end = TimeUtil.getCurrentMonth();
		Set<String> domainset = queryAllDomain(start, end);
		List<String> domains = new ArrayList<String>(domainset);
		Collections.sort(domains);

		for (String domain : domains) {
			processOneDomain(start, end, domain);
		}

		for (Entry<String, DomainInfo> entry : m_infos.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}

	private void processOneDomain(Date start, Date end, String domain) {
		System.out.println("process:" + domain);
		long startTime = start.getTime();
		long endTime = end.getTime();
		DomainInfo info = m_infos.get(domain);

		if (info == null) {
			info = new DomainInfo();

			m_infos.put(domain, info);
		}
		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			Date date = new Date(startTime);

			try {
				Dailyreport dailyreport = m_dailyreportDao.findByNameDomainPeriod(date, domain, "transaction",
				      DailyreportEntity.READSET_FULL);
				TransactionReport report = DefaultSaxParser.parse(dailyreport.getContent());

				info.reset(report.findMachine(CatString.ALL));
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(info);
	}

	public static class DomainInfo {

		private Indicator m_url = new Indicator();

		private Indicator m_service = new Indicator();

		public void reset(Machine machine) {
			Map<String, TransactionType> types = machine.getTypes();
			TransactionType url = types.get("URL");
			TransactionType service = types.get("Service");

			if (service == null) {
				service = types.get("PigeonService");
			}
			if (url != null) {
				m_url.reset(url);
			}
			if (service != null) {
				m_service.reset(service);
			}
		}

		public Indicator getUrl() {
			return m_url;
		}

		public void setUrl(Indicator url) {
			m_url = url;
		}

		public Indicator getService() {
			return m_service;
		}

		public void setService(Indicator service) {
			m_service = service;
		}

		@Override
		public String toString() {
			return m_url.toString() + m_service.toString();
		}
	}

	public static class Indicator {
		private long m_totalCount;

		private long m_sum;

		private double m_avg;

		private int m_days;

		public long getSum() {
			return m_sum;
		}

		public void reset(TransactionType type) {
			m_days++;
			m_totalCount += type.getTotalCount();
			m_sum += type.getSum();
			m_avg = (double) m_sum / m_totalCount;
		}

		public void setSum(long sum) {
			m_sum = sum;
		}

		public long getTotalCount() {
			return m_totalCount;
		}

		public void setTotalCount(long totalCount) {
			m_totalCount = totalCount;
		}

		public double getAvg() {
			return m_avg;
		}

		public void setAvg(double avg) {
			m_avg = avg;
		}

		@Override
		public String toString() {
			if (m_days == 0) {
				m_days = 1;
			}
			DecimalFormat nf = new DecimalFormat("0.0000");
			return new StringBuilder().append(m_days).append('\t')
			      .append(nf.format((double) m_totalCount / m_days / TimeUtil.ONE_DAY * 1000)).append('\t')
			      .append(nf.format(m_avg)).append('\t').toString();
		}
	}

}
