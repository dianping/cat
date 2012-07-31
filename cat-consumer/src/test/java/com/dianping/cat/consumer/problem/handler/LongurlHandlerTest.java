package com.dianping.cat.consumer.problem.handler;

import junit.framework.Assert;

import org.junit.Test;

public class LongurlHandlerTest {

	private LongUrlHandler m_handler;
	
	@Test
	public void testHandler(){
		m_handler = new LongUrlHandler();
		
		for(int i=0;i<1000;i++){
			Assert.assertEquals(-1, m_handler.getDuration(i, "domain"));
		}
		for(int i=1000;i<2000;i++){
			Assert.assertEquals(1000, m_handler.getDuration(i, "domain"));
		}
		for(int i=2000;i<3000;i++){
			Assert.assertEquals(2000, m_handler.getDuration(i, "domain"));
		}
		for(int i=3000;i<4000;i++){
			Assert.assertEquals(3000, m_handler.getDuration(i, "domain"));
		}
		for(int i=4000;i<5000;i++){
			Assert.assertEquals(4000, m_handler.getDuration(i, "domain"));
		}
		for(int i=5000;i<8000;i++){
			Assert.assertEquals(5000, m_handler.getDuration(i, "domain"));
		}
	}
}
