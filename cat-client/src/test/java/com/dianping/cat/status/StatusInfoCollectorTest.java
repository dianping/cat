package com.dianping.cat.status;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.status.model.entity.StatusInfo;

public class StatusInfoCollectorTest {
	@Test
	public void test() {
		StatusInfo status = new StatusInfo();

		status.accept(new StatusInfoCollector(null, null));

		Assert.assertEquals(true, status.getDisk() != null);
		Assert.assertEquals(true, status.getMemory() != null);
		Assert.assertEquals(true, status.getMessage().getBytes() >= 0);
		Assert.assertEquals(true, status.getMessage().getOverflowed() >= 0);
		Assert.assertEquals(true, status.getMessage().getProduced() >= 0);
		Assert.assertEquals(true, status.getOs() != null);
		Assert.assertEquals(true, status.getRuntime() != null);
		Assert.assertEquals(true, status.getThread() != null);
	}
}
