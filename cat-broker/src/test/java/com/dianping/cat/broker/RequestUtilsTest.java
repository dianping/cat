package com.dianping.cat.broker;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.broker.api.page.RequestUtils;

public class RequestUtilsTest {

	@Test
	public void test(){
		RequestUtils utils = new RequestUtils();
		
		Assert.assertEquals(null, utils.filterXForwardedForIP("10.1.6.128,10.1.6.128"));
		Assert.assertEquals(null, utils.filterXForwardedForIP("172.16.6.128,172.16.6.128"));
		Assert.assertEquals(null, utils.filterXForwardedForIP("172.31.6.128,172.31.6.128"));
		Assert.assertEquals(null, utils.filterXForwardedForIP("192.168.0.1,192.168.0.1"));
		Assert.assertEquals("172.32.6.128", utils.filterXForwardedForIP("172.32.6.128,172.32.6.128"));
	}

}
