package com.dianping.cat.abtest.spi.internal.conditions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserAgentParserTest {

	@Test
	public void test(){
		UserAgentParser parser = new UserAgentParser();

		 String header = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; T312461; .NET CLR 1.1.4322)";
		 
		 UserAgent ua = parser.parse(header);
		 
		 //System.out.println(ua);
       assertEquals("Windows NT 5.0", ua.getOs());
       assertEquals("MSIE", ua.getBrowser());
       assertEquals("6.0", ua.getBrowserVersion()); 
	}
}
