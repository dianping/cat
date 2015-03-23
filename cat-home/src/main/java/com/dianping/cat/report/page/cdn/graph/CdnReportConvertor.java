package com.dianping.cat.report.page.cdn.graph;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.IpService.IpInfo;

public class CdnReportConvertor extends BaseVisitor {
	private IpService m_ipService;

	private MetricReport m_report;

	private String m_cdn;

	private String m_province;

	private String m_city;

	private static final String ALL = "ALL";

	public CdnReportConvertor(IpService ipService) {
		m_ipService = ipService;
	}

	private String filterAndConvert(String cdn, String sip) {
		boolean isAllCdn = m_cdn.equals(ALL);

		if (isAllCdn || m_cdn.equals(cdn)) {
			IpInfo ipInfo = m_ipService.findIpInfoByString(sip);
			String province = ipInfo.getProvince();
			String city = ipInfo.getCity();

			if (m_province.equals(ALL)) {
				if (isAllCdn) {
					return cdn;
				} else {
					return province;
				}
			} else if (m_province.equals(province)) {
				if (m_city.equals(ALL) || m_city.equals(city)) {
					if (isAllCdn) {
						return cdn;
					} else {
						return city;
					}
				}
			}
		}
		return null;
	}

	public MetricReport getReport() {
		return m_report;
	}

	public void mergeMetricItem(MetricItem from, MetricItem to) {
		for (Segment temp : to.getSegments().values()) {
			Segment target = from.findOrCreateSegment(temp.getId());

			mergeSegment(target, temp);
		}
	}

	protected void mergeSegment(Segment old, Segment point) {
		old.setCount(old.getCount() + point.getCount());
		old.setSum(old.getSum() + point.getSum());
		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	public CdnReportConvertor setCdn(String cdn) {
		m_cdn = cdn;
		return this;
	}

	public CdnReportConvertor setCity(String city) {
		m_city = city;
		return this;
	}

	public CdnReportConvertor setProvince(String province) {
		m_province = province;
		return this;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		try {
			String id = metricItem.getId();
			String[] temp = id.split(":");
			String cdn = temp[2];
			String sip = temp[3].trim();
			String key = filterAndConvert(cdn, sip);

			if (key != null) {
				MetricItem item = m_report.findOrCreateMetricItem(key);

				mergeMetricItem(item, metricItem);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		m_report = new MetricReport(metricReport.getProduct());
		super.visitMetricReport(metricReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}
}
