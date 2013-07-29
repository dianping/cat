package com.dianping.cat.home.abtest.report;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.advanced.dal.BusinessReport;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.advanced.dal.BusinessReportEntity;
import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeParser;

public class RetinaImgBusinessReportTest extends ComponentTestCase {

	private BusinessReportDao m_businessReportDao;

	private Calendar m_calendar;

	@Before
	public void prepare() {
		try {
			m_businessReportDao = lookup(BusinessReportDao.class);
			m_calendar = Calendar.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		try {
			m_calendar.set(2013, 6, 16, 0, 0);
			Date begin = m_calendar.getTime();
			System.out.println(begin);
			m_calendar.set(2013, 6, 17, 0, 0);
			Date end = m_calendar.getTime();
			System.out.println(end);
			
			List<BusinessReport> reportsTuangou = m_businessReportDao.findAllByProductLineNameDuration(begin, end,
			      "TuanGou", "metric", BusinessReportEntity.READSET_FULL);

			List<BusinessReport> reportsPAY = m_businessReportDao.findAllByProductLineNameDuration(begin, end, "PAY",
			      "metric", BusinessReportEntity.READSET_FULL);

			Metric pair = new Metric();

			caculate(reportsTuangou, "/detail", pair,"xxx");
			caculate(reportsPAY, "/detail", pair,"xxx");

			System.out.println(String.format("Detail: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countA,
			      pair.m_countB, pair.m_sumA, pair.m_sumB));

			pair = new Metric();

			caculate(reportsTuangou, "/order/submitOrder", pair,"xxx");
			caculate(reportsPAY, "/order/submitOrder", pair,"xxx");
			
			System.out.println(String.format("/order/submitOrder: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countA,
			      pair.m_countB, pair.m_sumA, pair.m_sumB));

			pair = new Metric();

			caculate(reportsTuangou, "order", pair,"/order/submitOrder");
			caculate(reportsPAY, "order", pair,"/order/submitOrder");
			System.out.println(String.format("order: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countA,
			      pair.m_countB, pair.m_sumA, pair.m_sumB));

			pair = new Metric();
			
			System.out.println("------------------");

			caculate(reportsTuangou, "payment.pending", pair,"xxx");
			caculate(reportsPAY, "payment.pending", pair,"xxx");
			System.out.println(String.format("payment.pending: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countPayA,
			      pair.m_countPayB, pair.m_sumA, pair.m_sumB));
		} catch (DalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void caculate(List<BusinessReport> reports, String target, Metric pair, String excluded) {
		for (BusinessReport report : reports) {
			MetricReport metricReport = DefaultNativeParser.parse(report.getContent());

			//System.out.println(metricReport);
			Map<String, MetricItem> items = metricReport.getMetricItems();

			for (MetricItem item : items.values()) {
				//System.out.println(item.getId());
				if (item.getId().indexOf(target) > -1 && item.getId().indexOf(excluded) == -1) {
					//System.out.println("-----");
					Map<String, Abtest> abs = item.getAbtests();

					for (Abtest abtest : abs.values()) {
						Map<String, Group> groups = abtest.getGroups();

						for (Group group : groups.values()) {
							if (group.getName().equalsIgnoreCase("A")) {
								Map<Integer, Point> points = group.getPoints();

								for (Point point : points.values()) {
									//System.out.println(point.getSum());
									pair.m_countA += point.getCount();

									if (point.getAvg() % 39.9 == 0.0 || point.getAvg() % 29 == 0.0 || point.getAvg() % 128 == 0.0 || point.getAvg() % 198 == 0.0
									      || point.getAvg() % 98 == 0.0) {
										pair.m_countPayA += point.getCount();
										// System.out.println(point.getSum());
										pair.m_sumA += point.getSum();
									} else {
										//System.out.println("-----------");
										System.out.println(point.getAvg());
									}
								}
							} else if (group.getName().equalsIgnoreCase("B")) {
								Map<Integer, Point> points = group.getPoints();
								for (Point point : points.values()) {
									//System.out.println(point.getSum());
									pair.m_countB += point.getCount();
									/*
									 * point.getAvg() % 39.9 == 0.0 || point.getAvg() % 29 == 0.0
									      || point.getAvg() % 128 == 0.0 || point.getAvg() % 198 == 0.0
									      || point.getAvg() % 98 == 0.0
									 */
									if (point.getAvg() % 39.9 == 0.0 || point.getAvg() % 29 == 0.0 || point.getAvg() % 128 == 0.0 || point.getAvg() % 198 == 0.0
									      || point.getAvg() % 98 == 0.0) {
										pair.m_countPayB += point.getCount();
										// System.out.println(point.getSum());
										pair.m_sumB += point.getSum();
									} else {
										//System.out.println("-----------");
										System.out.println(point.getAvg());
									}

								}
							}
						}
					}
				} else {
					 //System.out.println(item.getId());
				}
			}
		}
	}

	public static class Metric {
		public int m_countA = 0;
		
		public int m_countPayA = 0;

		public int m_countB = 0;
		
		public int m_countPayB = 0;

		public double m_sumA = 0.0;

		public double m_sumB = 0.0;
	}
}
