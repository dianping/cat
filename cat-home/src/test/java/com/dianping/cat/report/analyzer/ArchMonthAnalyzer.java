package com.dianping.cat.report.analyzer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
public class ArchMonthAnalyzer extends ComponentTestCase {

	private Map<Long, Indicator> indicators = new LinkedHashMap<Long, Indicator>();

	@Inject
	private DailyreportDao m_dailyreportDao;

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
	public void builderData() throws IOException {
		Date start = TimeUtil.getLastMonth();
		start.setTime(start.getTime()+TimeUtil.ONE_DAY*16);
		Date end = TimeUtil.getCurrentDay();

		Set<String> domains = queryAllDomain(start, end);

		for (int i = 0; i < 31; i++) {
			Date date = new Date(start.getTime() + i * TimeUtil.ONE_DAY);
			System.out.println("process day " + date);
			processOneDay(date, domains);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (Entry<Long, Indicator> entry : indicators.entrySet()) {
			Date time = new Date(entry.getKey());
			Indicator indicator = entry.getValue();

			System.out.println(sdf.format(time) + "\t" + indicator);
		}
	}

	private void processOneDay(Date date, Set<String> domains) {
		for (String domain : domains) {
			try {
				Dailyreport report = m_dailyreportDao.findByNameDomainPeriod(date, domain, "transaction",
				      DailyreportEntity.READSET_FULL);

				TransactionReport transactionReport = DefaultSaxParser.parse(report.getContent());
				Machine machine = transactionReport.findOrCreateMachine(CatString.ALL);

				Indicator indicator = indicators.get(date.getTime());
				if (indicator == null) {
					indicator = new Indicator();
					indicators.put(date.getTime(), indicator);
				}
				indicator.accept(machine, transactionReport);
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class Indicator {
		private Item m_call = new Item();

		private Item m_kvdbCache = new Item();

		private Item m_memCache = new Item();

		private Item m_service = new Item();

		private Item m_sql = new Item();

		private Item m_url = new Item();

		private Item m_webCache = new Item();

		private boolean m_isDebug = true;

		public void accept(Machine machine, TransactionReport report) {
			Collection<TransactionType> types = machine.getTypes().values();
			for (TransactionType type : types) {
				String name = type.getId();
				long count = type.getTotalCount();
				long error = type.getFailCount();
				double sum = type.getSum();
				if (name.equalsIgnoreCase("url")) {
					m_url.add(count, error, sum);
					double avg = type.getAvg();
					if (m_isDebug) {
						if (avg > 90) {
							System.out.println(report.getDomain());
							System.out.println(count + " " + avg);
						}
					}
				} else if (name.equalsIgnoreCase("service") || name.equalsIgnoreCase("pigeonService")) {
					double avg = type.getAvg();
					if (m_isDebug) {
						if (avg > 10) {
							System.out.println(report.getDomain());
							System.out.println(count + " " + avg);
						}
					}
					m_service.add(count, error, sum);
				} else if (name.equalsIgnoreCase("call") || name.equalsIgnoreCase("pigeonCall")) {
					if (m_isDebug) {
						if (error > 1000) {
							System.out.println(report.getDomain() + ":" + error);
						}
					}
					m_call.add(count, error, sum);
				} else if (name.equalsIgnoreCase("sql")) {
					m_sql.add(count, error, sum);
				} else if (name.equalsIgnoreCase("Cache.kvdb")) {
					m_kvdbCache.add(count, error, sum);
				} else if (name.startsWith("Cache.memcached")) {
					m_memCache.add(count, error, sum);
					if (m_isDebug) {
						if (error > 1000) {
							System.out.println(report.getDomain());
							System.out.println(machine.getIp());
						}
					}
				} else if (name.equalsIgnoreCase("Cache.web")) {
					m_webCache.add(count, error, sum);
				}
			}
		}

		public Item getCall() {
			return m_call;
		}

		public Item getKvdbCache() {
			return m_kvdbCache;
		}

		public Item getMemCache() {
			return m_memCache;
		}

		public Item getService() {
			return m_service;
		}

		public Item getSql() {
			return m_sql;
		}

		public Item getUrl() {
			return m_url;
		}

		public Item getWebCache() {
			return m_webCache;
		}

		public void setCall(Item call) {
			m_call = call;
		}

		public void setKvdbCache(Item kvdbCache) {
			m_kvdbCache = kvdbCache;
		}

		public void setMemCache(Item memCache) {
			m_memCache = memCache;
		}

		public void setService(Item service) {
			m_service = service;
		}

		public void setSql(Item sql) {
			m_sql = sql;
		}

		public void setUrl(Item url) {
			m_url = url;
		}

		public void setWebCache(Item webCache) {
			m_webCache = webCache;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append("url" + "\t").append(m_url);
			sb.append("service" + "\t").append(m_service);
			sb.append("call" + "\t").append(m_call);
			sb.append("sql" + "\t").append(m_sql);
			sb.append("memcache" + "\t").append(m_memCache);
			sb.append("kvdb" + "\t").append(m_kvdbCache);
			sb.append("web" + "\t").append(m_webCache);
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
