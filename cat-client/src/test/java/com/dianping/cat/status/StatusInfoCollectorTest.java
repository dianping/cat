package com.dianping.cat.status;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.status.model.entity.StatusInfo;

public class StatusInfoCollectorTest {
	@Test
	public void test() {
		StatusInfo status = new StatusInfo();

		status.accept(new StatusInfoCollector(null));

		Assert.assertEquals(true, status.findExtension("DISK FREE") != null);
		Assert.assertEquals(true, status.findExtension("MEMORY") != null);
		Assert.assertEquals(true, status.findExtension("MEMORY").findDetail("Max") != null);
	}
}
