package com.dianping.cat.report.page.userMonitor;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.site.lookup.util.StringUtils;

public class UserMonitorConvert extends BaseVisitor {

	private MetricReport m_report;

	private String m_type;

	private String m_city;

	private String m_channel;

	public UserMonitorConvert(String type, String city, String channel) {
		m_type = type;
		m_city = city;
		m_channel = channel;
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

	public MetricReport getReport() {
		return m_report;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		try {
			String id = metricItem.getId();
			String[] temp = id.split(":");
			String city = temp[0];
			String channel = temp[1];
			String info = temp[2];

			if (validate(city, channel, info)) {
				MetricItem item = m_report.findOrCreateMetricItem(info);

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

	private boolean validate(String city, String channel, String info) {
		if ("info".equals(m_type)) {
			if (validateCity(city) && validateChannel(channel) && validateInfo(info)) {
				return true;
			}
		} else if ("httpStatus".equals(m_type)) {
			if (validateCity(city) && validateChannel(channel) && validateHttpStatus(info)) {
				return true;
			}
		} else if ("errorCode".equals(m_type)) {
			if (validateCity(city) && validateChannel(channel) && validateErrorCode(info)) {
				return true;
			}
		}
		return false;
	}

	private boolean validateInfo(String info) {
		if ("hit".equals(info) || "error".equals("info")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateHttpStatus(String info) {
		if (info.startsWith("httpStatus")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateErrorCode(String info) {
		if (info.startsWith("errorStatus")) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateCity(String city) {
		if (StringUtils.isEmpty(m_city) || city.equals(m_city)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateChannel(String channel) {
		if (StringUtils.isEmpty(m_channel) || channel.equals(m_channel)) {
			return true;
		} else {
			return false;
		}
	}

}
