package com.dianping.cat.report.page.cdn.graph;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.IpService.IpInfo;

public class CdnReportConvertor extends BaseVisitor {
	@Inject
	private CdnConfig m_cdnConfig;
	
	@Inject
	private IpService m_ipService;
	
	private MetricReport m_report;

	private String m_cdn;

	private String m_province;

	private String m_city;

	public void SetConventorParameter(String cdn, String province, String city) {
		m_cdn = cdn;
		m_province = province;
		m_city = city;
		
		if (province == "ALL") {
			m_city = "ALL";
		}
		if (cdn == "ALL") {
			m_province = "ALL";
			m_city = "ALL";
		}
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

	private String filterAndConvert(String vip, String sip) {
		String keyCdn, keyProvince, keyCity;
		String cdn = m_cdnConfig.getCdnName(vip);
		
		if (!m_cdn.equals("ALL") && !m_cdn.equals(cdn)) {
			return null;
		}
		
		IpInfo ipInfo = m_ipService.findIpInfoByString(sip);
		String province, city;
		if (ipInfo == null) {
			province = "未知";
			city = "未知";
		} else {
			province = ipInfo.getProvince();
			city = ipInfo.getCity();
			if (city.equals("")) {
				city = "未知";
			}
		}
		
		if (!m_province.equals("ALL") && !m_province.equals(province)) {
			return null;
		}
		if (!m_province.equals("ALL") && !m_city.equals("ALL") && !m_city.equals(city)) {
			return null;
		}
		
		keyCdn = cdn;
		keyProvince = province;
		keyCity = city;
		
		return keyCdn + ":" + keyProvince + ":" + keyCity + ":" + sip;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		try {
			String id = metricItem.getId();
			String[] temp = id.split(":");
			String vip = temp[2];
			String sip = temp[3];
			String key = filterAndConvert(vip, sip);
			
			if (key != null) {
				MetricItem item = m_report.findOrCreateMetricItem(key);

				mergeMetricItem(item, metricItem);
			}
		} catch (Exception e) {
			Cat.logError(e);
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
