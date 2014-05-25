package com.dianping.cat.report.page.userMonitor;

import org.hsqldb.lib.StringUtil;

import com.dianping.cat.Cat;
import com.dianping.cat.Monitor;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.entity.StatisticsItem;
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

	private void buildDetailInfo(MetricItem metricItem, String city, String channel, String info) {
	   int total = 0;

	   for (Segment segment : metricItem.getSegments().values()) {
	   	total = total + segment.getCount();
	   }

	   if (Monitor.TYPE_INFO.equals(m_type)) {
	   	String key = "";

	   	if (info.equals(Monitor.HIT)) {
	   		key = Monitor.HIT_COUNT;
	   	} else if (info.equals(Monitor.ERROR)) {
	   		key = Monitor.ERROR_COUNT;
	   	} else {
	   		return;
	   	}
	   	int index = city.indexOf('-');
	   	if (StringUtil.isEmpty(m_city)) {
	   		StatisticsItem tem = m_report.findOrCreateStatistic(Monitor.CITY + key).findOrCreateStatisticsItem(
	   		      city.substring(0, index));

	   		tem.setCount(tem.getCount() + total);
	   	} else {
	   		StatisticsItem tem = m_report.findOrCreateStatistic(Monitor.CITY + key).findOrCreateStatisticsItem(
	   		      city.substring(index + 1));

	   		tem.setCount(tem.getCount() + total);
	   	}
	   	if (StringUtil.isEmpty(m_channel)) {
	   		StatisticsItem tem = m_report.findOrCreateStatistic(Monitor.CHANNEL + key)
	   		      .findOrCreateStatisticsItem(channel);

	   		tem.setCount(tem.getCount() + total);
	   	}
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

	private boolean validate(String city, String channel, String info) {
		if (Monitor.TYPE_INFO.equals(m_type)) {
			if (validateCity(city) && validateChannel(channel) && validateInfo(info)) {
				return true;
			}
		} else if (Monitor.HTTP_STATUS.equals(m_type)) {
			if (validateCity(city) && validateChannel(channel) && validateHttpStatus(info)) {
				return true;
			}
		} else if (Monitor.ERROR_CODE.equals(m_type)) {
			if (validateCity(city) && validateChannel(channel) && validateErrorCode(info)) {
				return true;
			}
		}
		return false;
	}

	private boolean validateChannel(String channel) {
		if (StringUtils.isEmpty(m_channel) || channel.equals(m_channel)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateCity(String city) {
		if (StringUtils.isEmpty(m_city) || city.contains(m_city)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateErrorCode(String info) {
		if (info.startsWith(Monitor.ERROR_CODE)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateHttpStatus(String info) {
		if (info.startsWith(Monitor.HTTP_STATUS)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validateInfo(String info) {
		if (Monitor.AVG.equals(info) || Monitor.HIT.equals(info) || Monitor.ERROR.equals(info)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		try {
			String id = metricItem.getId();
			String[] temp = id.split(":");
			String city = temp[2];
			String channel = temp[3];
			String info = temp[4];

			if (validate(city, channel, info)) {
				MetricItem item = m_report.findOrCreateMetricItem(info);

				mergeMetricItem(item, metricItem);

				buildDetailInfo(metricItem, city, channel, info);
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
