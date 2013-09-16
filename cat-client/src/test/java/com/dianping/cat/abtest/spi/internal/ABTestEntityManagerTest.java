package com.dianping.cat.abtest.spi.internal;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.abtest.spi.ABTestEntity;

public class ABTestEntityManagerTest extends ComponentTestCase {

	private static final String PATTERN = "yyyy-MM-dd hh:mm:ss";

	@Test
	public void testGetEntity() throws Exception {
		checkEntity("-1", null, null, null, null, false, false);
		checkEntity("1001", "Mock1", "roundrobin", "This is the configuration", "2012-01-01 00:00:00", false, false);
		checkEntity("1001", "Mock1", "roundrobin", "This is the configuration", "2013-04-10 18:00:00", true, false);
	}

	private void checkEntity(final String name, String expectedEntityName, String expectedGroupStrategy,
	      String expectedGroupStrategyConfiguration, String expectedDateStr, boolean expectedEligible,
	      boolean expectedDisabled) throws Exception {
		ABTestEntityManager manager = lookup(ABTestEntityManager.class);
		ABTestEntity entity = null;

		for (ABTestEntity e : manager.getEntityList()) {
			if (e.getName() == name) {
				entity = e;
				break;
			}
		}

		if (entity == null) {
			return;
		}

		Assert.assertNotNull(entity);

		if (expectedGroupStrategy != null) {
			Assert.assertEquals(expectedGroupStrategy, entity.getGroupStrategyName());
		}

		if (expectedGroupStrategy != null) {
			Assert.assertEquals(expectedGroupStrategyConfiguration, entity.getGroupStrategyDescriptor());
		}

		Assert.assertEquals(expectedEntityName, entity.getName());

		if (expectedDateStr != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
			Date date = sdf.parse(expectedDateStr);
			Assert.assertEquals(expectedEligible, entity.isEligible(date));
		}

		Assert.assertEquals(expectedDisabled, entity.isDisabled());

	}
}
