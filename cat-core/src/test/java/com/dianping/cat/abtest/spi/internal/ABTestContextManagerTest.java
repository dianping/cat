package com.dianping.cat.abtest.spi.internal;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class ABTestContextManagerTest {
	
	@Test
	public void testDecoding(){
		DefaultABTestContextManager manager = new DefaultABTestContextManager();
		DefaultABTestContextManager.Entry entry = manager.new Entry();
		
		Map<String, Map<String, String>> map = entry.decode("1=ab:A|cd:B&2=ab:A|cd:B");
		Assert.assertEquals(map.toString(),"{2={ab=A, cd=B}, 1={ab=A, cd=B}}");
		
		Map<String, Map<String, String>> map2 = entry.decode("1=ab:|cd:B&2=ab:A|cd:B");
		Assert.assertEquals(map2.toString(),"{2={ab=A, cd=B}, 1={ab=, cd=B}}");
		
		Map<String, Map<String, String>> map3 = entry.decode("1=ab:A|cd:B&2=ab:A|cd:");
		Assert.assertEquals(map3.toString(),"{2={ab=A, cd=}, 1={ab=A, cd=B}}");
		
		Map<String, Map<String, String>> map4 = entry.decode("");
		Assert.assertEquals(map4.toString(),"{}");
	}
	
	@Test
	public void testEncoding(){
		DefaultABTestContextManager manager = new DefaultABTestContextManager();
		DefaultABTestContextManager.Entry entry = manager.new Entry();
		
		Map<String, Map<String, String>> map = entry.decode("1=ab:A|cd:B&2=ab:A|cd:B");
		Assert.assertEquals(entry.encode(map),"2=ab:A|cd:B&1=ab:A|cd:B");
		
		Map<String, Map<String, String>> map2 = entry.decode("1=ab:|cd:B&2=ab:A|cd:B");
		Assert.assertEquals(entry.encode(map2),"2=ab:A|cd:B&1=ab:|cd:B");
		
		Map<String, Map<String, String>> map3 = entry.decode("1=ab:A|cd:B&2=ab:A|cd:");
		Assert.assertEquals(entry.encode(map3),"2=ab:A|cd:&1=ab:A|cd:B");
		
		Map<String, Map<String, String>> map4 = entry.decode("");
		Assert.assertEquals(entry.encode(map4),"");
	}
}
