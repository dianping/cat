package com.dianping.cat.abtest.spi.internal;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.abtest.ABTestId;
import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.abtest.spi.ABTestEntityManager;

public class DefaultABTestEntityManagerTest extends ComponentTestCase {

	@Test
	public void testGetEntity() throws Exception {
		ABTestEntityManager abTestEntityManager = lookup(ABTestEntityManager.class);
		// 获取不到entity的情况
		ABTestId id = new ABTestId() {
			@Override
			public int getValue() {
				return -1;
			}
		};
		ABTestEntity entity = abTestEntityManager.getEntity(id);
		Assert.assertNotNull(entity);
		Assert.assertEquals(entity.getId(), 0);
		Assert.assertEquals(entity.isDisabled(), true);
		// 获取具体的entity
		id = new ABTestId() {
			@Override
			public int getValue() {
				return 1001;
			}
		};
		entity = abTestEntityManager.getEntity(id);
		Assert.assertNotNull(entity);
		Assert.assertEquals(entity.getId(), id.getValue());
		Assert.assertEquals(entity.getGroupStrategy(), "mock");
		Assert.assertEquals(entity.getGroupStrategyConfiguration(), "This is the configuration");
		Assert.assertEquals(entity.getName(), "Mock1");
		Assert.assertEquals(entity.isDisabled(), false);
		Calendar cal = Calendar.getInstance();
		cal.set(2013, 0, 1, 0, 0, 0);
		Date date = cal.getTime();
		Assert.assertEquals(entity.isEligible(date), false);
		cal.set(2013, 3, 10, 17, 1, 0);
		date = cal.getTime();
		Assert.assertEquals(entity.isEligible(date), true);
	}
}
