package com.dianping.cat.status;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.status.model.entity.StatusInfo;

public class StatusInfoCollectorTest {
	@Test
	public void test() {
		StatusInfo status = new StatusInfo();

		status.accept(new StatusInfoCollector(null, null));

		Assert.assertEquals(true, status.findProperty("DiskVolume") != null);
		Assert.assertEquals(true, status.findProperty("Max") != null);
		Assert.assertEquals(true, status.findProperty("Arch") != null);
	}
}
