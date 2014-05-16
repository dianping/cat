package com.dianping.cat.report.page.metric;

import java.io.File;

import org.junit.Test;
import org.unidal.eunit.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;

public class TestCode {

	@Test
	public void test() throws Exception {
		MetricReport report = new MetricReport("test");

		for (int i = 0; i < 800; i++) {
			report.addMetricItem(create(i));
		}
		Files.forIO().writeTo(new File("/tmp/data.txt"), report.toString());
	}

	public MetricItem create(int i) {
		String key = getKey(i);
		MetricItem item = new MetricItem(key);

		for (int index = 0; index < 60; index++) {
			Segment segment = item.findOrCreateSegment(index);

			if (key.indexOf("南京") > -1) {
				if (key.indexOf("移动") > -1) {
					segment.setCount(index * 10);
					segment.setAvg(index);
					segment.setSum(index * index * 10);
				} else {
					segment.setCount(index);
					segment.setAvg(index * 10);
					segment.setSum(index * index * 10);
				}
			} else {
				if (key.indexOf("移动") > -1) {
					segment.setCount(index * 10 * 10);
					segment.setAvg(index);
					segment.setSum(index * index * 10 * 10);
				} else {
					segment.setCount(index * 10);
					segment.setAvg(index * 10);
					segment.setSum(index * index * 10 * 10);
				}
			}
		}
		return item;
	}

	public String getKey(int i) {
		if (i < 100) {
			return "江苏 南京:移动:httpStatus|200";
		} else if (i < 200) {
			return "江苏 南京:移动:httpStatus|300";
		} else if (i < 300) {
			return "江苏 南京:移动:httpStatus|400";
		} else if (i < 400) {
			return "江苏 南京:联通:httpStatus|200";
		} else if (i < 500) {
			return "江苏 南京:联通:httpStatus|300";
		} else if (i < 600) {
			return "江苏 扬州:联通:httpStatus|200";
		} else if (i < 700) {
			return "江苏 扬州:联通:httpStatus|300";
		} else {
			return "江苏 扬州:联通:httpStatus|400";
		}

	}
}
