package com.dianping.cat.report.page.business.graph;

import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.consumer.business.model.transform.BaseVisitor;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;

public class BusinessDataFetcher {

	@Inject
	private BusinessKeyHelper m_keyHelper;

	public Map<String, double[]> buildGraphData(BusinessReport businessReport) {
		BusinessDataBuilder builder = new BusinessDataBuilder();

		builder.visitBusinessReport(businessReport);
		return builder.getDatas();
	}

	public class BusinessDataBuilder extends BaseVisitor {

		private Map<String, double[]> m_datas = new LinkedHashMap<String, double[]>();

		private String m_domain;

		@Override
		public void visitBusinessReport(BusinessReport report) {
			m_domain = report.getDomain();
			super.visitBusinessReport(report);
		}

		@Override
		public void visitBusinessItem(BusinessItem item) {
			String key = item.getId();

			double[] sum = new double[60];
			double[] count = new double[60];
			double[] avg = new double[60];

			for (Segment seg : item.getSegments().values()) {
				int index = seg.getId();

				sum[index] = seg.getSum();
				count[index] = seg.getCount();
				avg[index] = seg.getAvg();
			}

			m_datas.put(m_keyHelper.generateKey(key, m_domain, MetricType.SUM.getName()), sum);
			m_datas.put(m_keyHelper.generateKey(key, m_domain, MetricType.COUNT.getName()), count);
			m_datas.put(m_keyHelper.generateKey(key, m_domain, MetricType.AVG.getName()), avg);
		}

		public Map<String, double[]> getDatas() {
			return m_datas;
		}
	}

}
