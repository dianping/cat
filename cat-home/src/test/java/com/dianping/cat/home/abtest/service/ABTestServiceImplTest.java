package com.dianping.cat.home.abtest.service;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.system.page.abtest.service.ABTestService;

public class ABTestServiceImplTest extends ComponentTestCase{

	@Test
	public void testGetNameById() throws Exception{
		ABTestService service = lookup(ABTestService.class);
		
		//String name = service.getABTestNameByRunId(61);
		
		//System.out.println(name);
	}
}

