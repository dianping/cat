package com.dianping.cat.report.page.metric;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.unidal.eunit.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.userMonitor.UserMonitorConvert;

public class TestCode {

	public MetricReport hackForTest(String product, Map<String, String> properties) {
		MetricReport report = null;
		try {
			String content = Files.forIO().readFrom(new File("/tmp/data.txt"), "utf-8");

			report = DefaultSaxParser.parse(content);

			report.setProduct(product);

		} catch (Exception e) {
			e.printStackTrace();
		}

		String city = properties.get("city");
		String channel = properties.get("channel");
		String type = properties.get("type");

		UserMonitorConvert convert = new UserMonitorConvert(type, city, channel);

		convert.visitMetricReport(report);

		return convert.getReport();
	}

	@Test
	public void test() throws Exception {
		MetricReport report = new MetricReport("id1");

		for (int i = 0; i < 800; i++) {
			report.addMetricItem(create(getKey1(i), i));
			report.addMetricItem(create(getKey2(i), i));
			report.addMetricItem(create(getKey3(i), i));
		}
		Files.forIO().writeTo(new File("/tmp/data.txt"), report.toString());
	}

	public MetricItem create(String key, int i) {
		MetricItem item = new MetricItem(key);

		for (int in = 0; in < 60; in++) {
			Segment segment = item.findOrCreateSegment(in);

			int index = 10;
			if (key.indexOf("南京") > -1) {
				if (key.indexOf("中国移动") > -1) {
					segment.setCount((int) (index * 10 * Math.random()));
					segment.setAvg(index * Math.random());
					segment.setSum(index * index * 10 * Math.random());
				} else {
					segment.setCount(index);
					segment.setAvg(index * 10 * Math.random());
					segment.setSum(index * index * 10 * Math.random());
				}
			} else {
				index = 100;
				if (key.indexOf("中国移动") > -1) {
					segment.setCount((int) (index * 10 * 10 * Math.random()));
					segment.setAvg(index * Math.random());
					segment.setSum(index * index * 10 * 10 * Math.random());
				} else {
					segment.setCount((int) (index * 10 * Math.random()));
					segment.setAvg(index * 10 * Math.random());
					segment.setSum(index * index * 10 * 10 * Math.random());
				}
			}
		}
		return item;
	}

	public String getKey1(int i) {
		if (i < 100) {
			return "broker-service:Metric:江苏 南京:中国移动:httpStatus|200";
		} else if (i < 200) {
			return "broker-service:Metric:江苏 南京:中国移动:httpStatus|300";
		} else if (i < 300) {
			return "broker-service:Metric:江苏 南京:中国移动:httpStatus|400";
		} else if (i < 400) {
			return "broker-service:Metric:江苏 南京:中国联通:httpStatus|200";
		} else if (i < 500) {
			return "broker-service:Metric:江苏 南京:中国联通:httpStatus|300";
		} else if (i < 600) {
			return "broker-service:Metric:江苏 扬州:中国联通:httpStatus|200";
		} else if (i < 700) {
			return "broker-service:Metric:江苏 扬州:中国联通:httpStatus|300";
		} else {
			return "broker-service:Metric:江苏 扬州:中国联通:httpStatus|400";
		}
	}

	public String getKey3(int i) {
		if (i < 100) {
			return "broker-service:Metric:江苏 南京:中国移动:errorCode|200";
		} else if (i < 200) {
			return "broker-service:Metric:江苏 南京:中国移动:errorCode|300";
		} else if (i < 300) {
			return "broker-service:Metric:江苏 南京:中国移动:errorCode|400";
		} else if (i < 400) {
			return "broker-service:Metric:江苏 南京:中国联通:errorCode|200";
		} else if (i < 500) {
			return "broker-service:Metric:江苏 南京:中国联通:errorCode|300";
		} else if (i < 600) {
			return "broker-service:Metric:江苏 扬州:中国联通:errorCode|200";
		} else if (i < 700) {
			return "broker-service:Metric:江苏 扬州:中国联通:errorCode|300";
		} else {
			return "broker-service:Metric:江苏 扬州:中国联通:errorCode|400";
		}
	}

	public String getKey2(int i) {
		if (i < 100) {
			return "broker-service:Metric:江苏 南京:中国移动:hit";
		} else if (i < 200) {
			return "broker-service:Metric:江苏 南京:中国移动:hit";
		} else if (i < 300) {
			return "broker-service:Metric:江苏 南京:中国移动:error";
		} else if (i < 400) {
			return "broker-service:Metric:江苏 南京:中国联通:error";
		} else if (i < 500) {
			return "broker-service:Metric:江苏 南京:中国联通:hit";
		} else if (i < 600) {
			return "broker-service:Metric:江苏 扬州:中国联通:hit";
		} else if (i < 700) {
			return "broker-service:Metric:江苏 扬州:中国联通:error";
		} else {
			return "broker-service:Metric:江苏 扬州:中国联通:error";
		}

	}
}
