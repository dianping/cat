package com.dianping.cat.influxdb;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.server.MetricService;
import com.dianping.cat.server.MetricType;
import com.dianping.cat.server.QueryParameter;

public class MetricServiceImplTest extends ComponentTestCase {

	private MetricService m_metricService;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_metricService = lookup(MetricService.class, InfluxDB.ID);
	}

	@Test
	public void test() throws Exception {
		Date start = m_sdf.parse("2016021307");
		Date end = m_sdf.parse("2016021308");
		QueryParameter parameter = new QueryParameter();

		parameter.setCategory("system").setStart(start).setEnd(end).setMeasurement("userCpu").setType(MetricType.AVG)
		      .setInterval("1m");

		Map<Long, Double> result = m_metricService.query(parameter);

		System.out.println(result);
	}

}
