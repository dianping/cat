package com.dianping.cat.system.alarm.template;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.home.template.entity.ThresholdTemplate;
import com.dianping.cat.home.template.transform.DefaultDomParser;
import com.dianping.cat.system.alarm.alert.AlertInfo;
import com.dianping.cat.system.alarm.threshold.ThresholdRule;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;

public class ThresholdRuleTest {
	private ThresholdRule m_rule;

	private Date m_lastDate;

	@Before
	public void setUp() {
		try {
			String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template.xml"), "utf-8");
			ThresholdTemplate template = new DefaultDomParser().parse(oldXml);

			m_rule = new ThresholdRule(1, "Cat", template);
		} catch (Exception e) {
		}
		List<ThresholdDataEntity> datas = m_rule.getDatas();

		prepareDatas(datas);
	}

	@Test
	public void testAddData() throws Exception {
		ThresholdDataEntity entity = new ThresholdDataEntity();

		entity.setCount(420);
		entity.setDate(new Date(m_lastDate.getTime() + 10));

		ThresholdAlarmMeta meta = m_rule.addData(entity, AlertInfo.EXCEPTION);

		Assert.assertEquals(240, meta.getRealCount());

		Map<String, Long> lastAlarmTimes = m_rule.getLastAlarmTime();
		int size = lastAlarmTimes.size();
		Assert.assertEquals(1, size);

		meta = m_rule.addData(entity, AlertInfo.EXCEPTION);
		Assert.assertEquals(null, meta);
	}

	@Test
	public void testConnectUrl() throws Exception {
		Assert.assertEquals("http://cat.dianpingoa.com/cat/r/dashboard?report=problem&type=error&domain=Cat",
		      m_rule.getConnectUrl());

	}

	@Test
	public void testCleanData() {
		int maxInterval = m_rule.getMaxInterval();
		m_rule.cleanData(maxInterval, m_lastDate.getTime());

		List<ThresholdDataEntity> datas = m_rule.getDatas();

		Assert.assertEquals(6, maxInterval);
		Assert.assertEquals(15, datas.size());
	}

	@Test
	public void testGetCount() {
		long count = m_rule.getCount(5, m_lastDate);
		Assert.assertEquals(180, count);

		count = m_rule.getCount(4, m_lastDate);
		Assert.assertEquals(140, count);

		count = m_rule.getCount(3, m_lastDate);
		Assert.assertEquals(100, count);

		List<ThresholdDataEntity> datas = m_rule.getDatas();
		int length = datas.size();

		ThresholdDataEntity entity = datas.get(length - 1);

		Date date = entity.getDate();
		ThresholdDataEntity entity1 = new ThresholdDataEntity();
		entity1.setCount(10);
		entity1.setDate(new Date(date.getTime() + 1));

		datas.add(entity1);

		ThresholdDataEntity entity2 = new ThresholdDataEntity();
		entity2.setCount(50);
		entity2.setDate(new Date(date.getTime() + 1));

		datas.add(entity2);
		count = m_rule.getCount(3, m_lastDate);
		Assert.assertEquals(150, count);

	}

	private void prepareDatas(List<ThresholdDataEntity> datas) {
		int size = 20;
		for (int i = 0; i < size; i++) {
			ThresholdDataEntity entity = new ThresholdDataEntity();
			entity.setCount((1 + i) * 20);

			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.MINUTE, -10);
			cal.add(Calendar.MILLISECOND, 30 * 1000 * i);
			Date date = cal.getTime();
			entity.setDate(date);
			entity.setDomain("Cat");

			datas.add(entity);

			if (i == size - 1) {
				m_lastDate = date;
			}
		}
	}
}
