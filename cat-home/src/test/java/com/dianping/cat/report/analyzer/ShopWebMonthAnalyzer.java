package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.view.StringSortHelper;

@RunWith(JUnit4.class)
public class ShopWebMonthAnalyzer extends ComponentTestCase {

	@Inject
	private DailyreportDao m_dailyreportDao;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

	private Map<Long, Indicator> urls = new LinkedHashMap<Long, Indicator>();

	public Indicator findOrCreat(Long time) {
		Indicator in = urls.get(time);
		if (in == null) {
			in = new Indicator();
			urls.put(time, in);
		}
		return in;
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_dailyreportDao = lookup(DailyreportDao.class);
	}

	@Test
	public void test() {
		try {
			String date = "2012/11/01";
			long start = sdf.parse(date).getTime();
			Date currentDay = TimeUtil.getCurrentDay();

			List<String> ips = new ArrayList<String>();
			Set<String> ipSets = new HashSet<String>();

			for (; start < currentDay.getTime(); start += TimeUtil.ONE_DAY) {
				System.out.println("Process" + new Date(start));
				try {
					Dailyreport dailyreport = m_dailyreportDao.findByNameDomainPeriod(new Date(start), "ShopWeb",
					      "transaction", DailyreportEntity.READSET_FULL);

					TransactionReport report = com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser
					      .parse(dailyreport.getContent());

					Collection<Machine> machines = report.getMachines().values();

					Indicator in = findOrCreat(start);
					in.setDate(new Date(start));
					Map<String, Long> temp = in.getUrls();
					for (Machine machine : machines) {
						ipSets.add(machine.getIp());
						temp.put(machine.getIp(), machine.findOrCreateType("URL").getTotalCount());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			ips = StringSortHelper.sort(ipSets);
			System.out.print("日期");
			for (String ip : ips) {
				System.out.print(ip + "\t");
			}
			System.out.println();
			for (Entry<Long, Indicator> entry : urls.entrySet()) {
				System.out.print(sdf.format(new Date(entry.getKey())) + "\t");

				Map<String, Long> value = entry.getValue().getUrls();
				for (String ip : ips) {
					System.out.print(value.get(ip) + "\t");
				}
				System.out.println();

			}
		} catch (Exception e) {
			System.out.println("BuildError" + e);
		}
	}

	public class Indicator {
		public Date m_date;

		public Map<String, Long> m_urls = new LinkedHashMap<String, Long>();

		public Date getDate() {
			return m_date;
		}

		public void setDate(Date date) {
			m_date = date;
		}

		public Map<String, Long> getUrls() {
			return m_urls;
		}

		public void setUrls(Map<String, Long> urls) {
			m_urls = urls;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(sdf.format(m_date)).append("\t");

			return sb.toString();
		}
	}

}
