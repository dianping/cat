package com.dianping.cat.status;

import org.junit.Test;

import com.dianping.cat.status.model.entity.StatusInfo;

public class StatusInfoCollectorTest {
	@Test
	public void test() {
		StatusInfo status = new StatusInfo();

		status.accept(new StatusInfoCollector(null,null));

		System.out.println(status);
	}
}
