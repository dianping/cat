package com.dianping.cat.report.analyzer;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.entity.MachineInfo;
import com.dianping.cat.consumer.health.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.report.view.StringSortHelper;

@RunWith(JUnit4.class)
public class MachineMonthAnalyzer extends ComponentTestCase {

	private Map<String, MonthDomain> m_indicators = new LinkedHashMap<String, MonthDomain>();

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_dailyreportDao = lookup(DailyreportDao.class);
	}

	private List<String> queryAllDomain(Date start, Date end) {
		Set<String> domains = new LinkedHashSet<String>();

		try {
			List<Dailyreport> reports = m_dailyreportDao.findAllDomainsByNameDuration(start, end, "health",
			      DailyreportEntity.READSET_DOMAIN_NAME);

			for (Dailyreport report : reports) {
				domains.add(report.getDomain());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return StringSortHelper.sort(domains);
	}

	@Test
	public void builderData() throws IOException {
		Date start = TimeUtil.getLastMonth();
		Date end = TimeUtil.getCurrentMonth();

		List<String> domains = queryAllDomain(start, end);

		int day = 31;
		for (int i = 0; i < day; i++) {
			Date date = new Date(start.getTime() + i * TimeUtil.ONE_DAY);
			System.out.println("process day " + date);
			processOneDay(date, domains);
		}

		System.out.print("项目名称" + "\t");
		System.out.print("部署机器" + "\t");
		System.out.print("平均负载" + "\t");
		System.out.println("最大负载" + "\t");

		for (Entry<String, MonthDomain> entry : m_indicators.entrySet()) {
			MonthDomain monthInfo = entry.getValue();

			System.out.println(entry.getKey() + "\t" + monthInfo);
		}
	}

	private void processOneDay(Date date, List<String> domains) {
		for (String domain : domains) {
			try {
				Dailyreport report = m_dailyreportDao.findByNameDomainPeriod(date, domain, "health",
				      DailyreportEntity.READSET_FULL);
				HealthReport healthReport = DefaultSaxParser.parse(report.getContent());
				MachineInfo machine = healthReport.getMachineInfo();
				MonthDomain indicator = m_indicators.get(domain);
				if (indicator == null) {
					indicator = new MonthDomain();
					m_indicators.put(domain, indicator);
				}
				DomainInfo t = indicator.findOrCreatDomain(date);
				t.add(machine.getNumbers(), machine.getAvgMaxLoad(), machine.getAvgLoad());
			} catch (DalNotFoundException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public class MonthDomain {
		private Map<Long, DomainInfo> m_infos = new LinkedHashMap<Long, DomainInfo>();

		public DomainInfo find(Date date) {
			return m_infos.get(date.getTime());
		}

		public DomainInfo findOrCreatDomain(Date date) {
			DomainInfo info = m_infos.get(date.getTime());
			if (info == null) {
				info = new DomainInfo();
				m_infos.put(date.getTime(), info);
			}
			return info;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			DomainInfo info = new DomainInfo();
			for (DomainInfo temp : m_infos.values()) {
				info.merger(temp);
			}
			sb.append(info);
			return sb.toString();
		}
	}

	public class DomainInfo {

		private int m_machine;

		private double m_loadSum;

		private double m_loadAvg;

		private double m_maxLoad;

		private double m_maxLoadSum;

		private int m_days;

		public DomainInfo merger(DomainInfo info) {
			m_machine = info.getMachine();
			m_loadSum += info.getLoadSum();
			m_days++;
			if (info.getMaxLoad() > m_maxLoad) {
				m_maxLoad = info.getMaxLoad();
			}
			m_maxLoadSum += info.getMaxLoadSum();
			return this;
		}

		public double getMaxLoadSum() {
			return m_maxLoadSum;
		}

		public void add(int machine, double maxLoad, double loadAvg) {
			m_machine = machine;
			m_loadSum += loadAvg;
			m_loadAvg = loadAvg;
			m_maxLoad = maxLoad;
			m_maxLoadSum = maxLoad;
			m_days = 1;
		}

		public int getMachine() {
			return m_machine;
		}

		public void setMachine(int machine) {
			m_machine = machine;
		}

		public double getLoadSum() {
			return m_loadSum;
		}

		public void setLoadSum(double loadSum) {
			m_loadSum = loadSum;
		}

		public double getMaxLoad() {
			return m_maxLoad;
		}

		public void setMaxLoad(double maxLoad) {
			m_maxLoad = maxLoad;
		}

		public double getLoadAvg() {
			return m_loadAvg;
		}

		public void setLoadAvg(double loadAvg) {
			m_loadAvg = loadAvg;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append(m_machine).append("\t").append(m_loadSum / m_days).append("\t").append(m_maxLoadSum / m_days)
			      .append("\t");
			return sb.toString();
		}
	}
}
