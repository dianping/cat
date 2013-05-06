package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;

@RunWith(JUnit4.class)
public class CatMonthAnalyzer extends ComponentTestCase {

	@Inject
	private DailyreportDao m_dailyreportDao;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

	private Indicator m_indicator= new Indicator();

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

			int i = 0;
			for (; start < currentDay.getTime(); start += TimeUtil.ONE_DAY) {
				i++;
				try {
					Dailyreport dailyreport = m_dailyreportDao.findByNameDomainPeriod(new Date(start), "Cat",
					      "transaction", DailyreportEntity.READSET_FULL);

					TransactionReport report = com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser
					      .parse(dailyreport.getContent());

					Machine machine = report.findOrCreateMachine(CatString.ALL);
					
					TransactionType type =machine.findOrCreateType("URL");
					
					Collection<TransactionName> names = type.getNames().values();
					for(TransactionName name:names){
						String id = name.getId();
						if(id.equalsIgnoreCase("model")){
						}else if(id.equalsIgnoreCase("dashboard")){
							m_indicator.mergeReboot(name);
						}else{
							m_indicator.mergePeople(name);
						}
					}
					System.out.println(sdf.format(new Date(start))+"\t"+m_indicator);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println(m_indicator.getPeople().getTotalCount()/(double)i);
			System.out.println(m_indicator.getApi().getTotalCount()/(double)i);
		} catch (Exception e) {
			System.out.println("BuildError" + e);
		}
	}

	public class Indicator {
		public Date m_date;

		public TransactionName m_people = new TransactionName("People");

		public TransactionName m_api = new TransactionName("Api");

		public Date getDate() {
			return m_date;
		}

		public void setDate(Date date) {
			m_date = date;
		}
		
		public void mergePeople(TransactionName name){
			mergeName(m_people, name);
		}

		public void mergeReboot(TransactionName name){
			mergeName(m_api, name);
		}
		
		
		public TransactionName getPeople() {
			return m_people;
		}

		public TransactionName getApi() {
			return m_api;
		}

		private void mergeName(TransactionName old, TransactionName other) {
			old.setTotalCount(old.getTotalCount() + other.getTotalCount());
			old.setFailCount(old.getFailCount() + other.getFailCount());

			if (other.getMin() < old.getMin()) {
				old.setMin(other.getMin());
			}

			if (other.getMax() > old.getMax()) {
				old.setMax(other.getMax());
			}

			old.setSum(old.getSum() + other.getSum());
			old.setSum2(old.getSum2() + other.getSum2());
			old.setLine95Value(0);
			if (old.getTotalCount() > 0) {
				old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
				old.setAvg(old.getSum() / old.getTotalCount());
			}

			if (old.getSuccessMessageUrl() == null) {
				old.setSuccessMessageUrl(other.getSuccessMessageUrl());
			}

			if (old.getFailMessageUrl() == null) {
				old.setFailMessageUrl(other.getFailMessageUrl());
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(m_people.getTotalCount()).append("\t");
			sb.append(m_people.getAvg()).append("\t");
			sb.append(m_api.getTotalCount()).append("\t");
			sb.append(m_api.getAvg()).append("\t");
			
			return sb.toString();
		}
	}

}
