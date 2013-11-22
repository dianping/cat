package com.dianping.cat.abtest.spi.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class ABTestCodecTest extends ComponentTestCase {
	@Test
	public void testCodec() throws Exception {
		check("1=ab:A|cd:B&2=ab:A|cd:B", "1=ab:A|cd:B&2=ab:A|cd:B");
		check("1=ab:|cd:B&2=ab:A|cd:B", "1=ab:|cd:B&2=ab:A|cd:B");
		check("1=ab:A|cd:B&2=ab:A|cd:", "1=ab:A|cd:B&2=ab:A|cd:");
		check("1=ab:|cd:B&2=ab:A|cd:", "1=ab:|cd:B&2=ab:A|cd:");
		check("1=ab:|cd:", "1=ab:|cd:");
		check("1=ab:|cd:A", "1=ab:|cd:A");
		check("", "");

		check("1=ab:A|cd:B&2=ab:A|cd:B", "1=ab:A|cd:B&2=ab:A|cd:B", "1", "2");
		check("1=ab:A|cd:B&2=ab:A|cd:B", "1=ab:A|cd:B", "1");
	}

	private void check(String source, String expected, String... keys) throws Exception {
		ABTestCodec codec = lookup(ABTestCodec.class);
		Map<String, Map<String, String>> map = codec.decode(source,
		      keys.length == 0 ? null : new HashSet<String>(Arrays.asList(keys)));

		Assert.assertEquals(expected, codec.encode(map));
	}
	
	@Test
	public void testCodec2() throws Exception{
		check2("1=ab:A|cd:B&2=ab:A|cd:B", "{1=A, 2=A}");
		check2("1=ab:A&2=ab:A|cd:B", "{1=A, 2=A}");
		check2("1=ab:A|cd:B&2=ab:|cd:B", "{1=A, 2=}");
		check2("1=cd:B&2=ab:A|cd:B", "{2=A}");
		check2("30=l:A|ab:A", "{30=A}");
		check2("1=ab:|cd:", "{1=}");
		check2("1=ab:|cd:A", "{1=}");
		check2("", "{}");
	}
	
	private void check2(String source, String expected) throws Exception {
		ABTestCodec codec = lookup(ABTestCodec.class);
		Map<String, String> map = codec.decode(source);

		Assert.assertEquals(expected, map.toString());
	}
	
	@Test
	public void testCodec3() throws Exception{
		check3("1", "1=ab:A");
	}
	
	private void check3(String runId, String source) throws Exception {
		ABTestCodec codec = lookup(ABTestCodec.class);
		 Map<String, Map<String, String>> map = codec.decode(source,null);
		 
		System.out.println(map.get(runId));
		
		String actual = codec.encode(runId,map.get(runId));

		Assert.assertEquals(source, actual);
	}
}
