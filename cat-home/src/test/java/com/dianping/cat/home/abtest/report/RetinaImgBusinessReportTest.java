package com.dianping.cat.home.abtest.report;

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

	@Before
	public void prepare() {
		try {
			m_businessReportDao = lookup(BusinessReportDao.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		try {
			List<BusinessReport> reports = m_businessReportDao.findAllByProductLineId(19437, "TuanGou",
			      BusinessReportEntity.READSET_FULL);

			Metric pair = caculate(reports, "/detail");

			System.out.println(String.format("Detail: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countA,
			      pair.m_countB, pair.m_sumA, pair.m_sumB));

			reports = m_businessReportDao.findAllByProductLineId(19437, "PAY", BusinessReportEntity.READSET_FULL);

			pair = caculate(reports, "/order/submitOrder");
			System.out.println(String.format("/order/submitOrder: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countA,
			      pair.m_countB, pair.m_sumA, pair.m_sumB));

			pair = caculate(reports, "order");
			System.out.println(String.format("order: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countA,
			      pair.m_countB, pair.m_sumA, pair.m_sumB));

			pair = caculate(reports, "payment.pending");
			System.out.println(String.format("payment.pending: A = %d, B = %d, Sum_A = %f, Sum_B = %f", pair.m_countA,
			      pair.m_countB, pair.m_sumA, pair.m_sumB));
		} catch (DalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Metric caculate(List<BusinessReport> reports, String target) {
		Metric pair = new Metric();

		for (BusinessReport report : reports) {
			MetricReport metricReport = DefaultNativeParser.parse(report.getContent());

			// System.out.println(metricReport);
			Map<String, MetricItem> items = metricReport.getMetricItems();

			for (MetricItem item : items.values()) {
				if (item.getId().equals(target)) {
					Map<String, Abtest> abs = item.getAbtests();

					for (Abtest abtest : abs.values()) {
						Map<String, Group> groups = abtest.getGroups();

						for (Group group : groups.values()) {
							if (group.getName().equalsIgnoreCase("A")) {
								Map<Integer, Point> points = group.getPoints();

								for (Point point : points.values()) {
									pair.m_countA += point.getCount();

									if (point.getAvg() % 39.9 == 0.0 || point.getAvg() % 29 == 0.0) {
										//System.out.println(point.getSum());
										pair.m_sumA += point.getSum();
									}
								}
							} else if (group.getName().equalsIgnoreCase("B")) {
								Map<Integer, Point> points = group.getPoints();

								for (Point point : points.values()) {
									pair.m_countB += point.getCount();

									if (point.getAvg() % 39.9 == 0.0 || point.getAvg() % 29 == 0.0) {
										//System.out.println(point.getSum());
										pair.m_sumB += point.getSum();
									}

								}
							}
						}
					}
				}
			}
		}

		return pair;
	}

	class Metric {
		public int m_countA = 0;

		public int m_countB = 0;

		public double m_sumA = 0.0;

		public double m_sumB = 0.0;
	}
}
