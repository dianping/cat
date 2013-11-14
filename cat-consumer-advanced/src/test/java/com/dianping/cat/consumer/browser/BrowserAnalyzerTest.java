package com.dianping.cat.consumer.browser;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

public class BrowserAnalyzerTest extends ComponentTestCase {

	// @SuppressWarnings("deprecation")
	// @Test
	// public void splitAgentTest() throws Exception {
	// BrowserAnalyzer browserAnalyzer = new BrowserAnalyzer();
	// String s = "Agent=Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)";
	//
	// String[] strings = browserAnalyzer.splitAgent(s);
	// String[] supposed = { "Mozilla/4.0", "compatible", "MSIE 8.0",
	// "Windows NT 5.1", "Trident/4.0", ".NET CLR 2.0.50727" };
	// Assert.assertEquals("Check the split result!", strings, supposed);
	// }
	//
	// @Test
	// public void updateTest() throws Exception {
	// BrowserAnalyzer browserAnalyzer = new BrowserAnalyzer();
	// String s =
	// "Agent=Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.4.2.1000 Chrome/18.0.966.0 Safari/535.12";
	// BrowserReport actual = browserAnalyzer
	// .update(new BrowserReport("Cat"), browserAnalyzer.splitAgent(s));
	// System.out.println(actual);
	// }

	// @Test
	// public void updateBrowserReportTest() throws Exception {
	// BrowserAnalyzer browserAnalyzer = new BrowserAnalyzer();
	// String s1 =
	// "Agent=Mozilla/5.0 (Linux; U; Android 4.0.3; zh-CN; WM8850-mid Build/IML74K) AppleWebKit/534.31 (KHTML, like Gecko) UCBrowser/9.2.4.329 U3/0.8.0 Mobile Safari/534.31";
	// String s2 =
	// "Agent=Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.4.2.1000 Chrome/18.0.966.0 Safari/535.12";
	// BrowserReport report1 = browserAnalyzer
	// .update(new BrowserReport("Cat"), browserAnalyzer.splitAgent(s1));
	// report1 = browserAnalyzer
	// .update(report1, browserAnalyzer.splitAgent(s2));
	// // Assert.assertEquals("Check the split result!", report1.toString()
	// // .replace("\r", ""), supposed.toString().replace("\r", ""));
	// }

	@Test
	public void testParseValue() {
		BrowserAnalyzer browserAnalyzer = new BrowserAnalyzer();
		String data = "RemoteIP=10.1.1.109&VirtualIP=10.1.1.109&Server=cat.dianpingoa.com&Referer=http://cat.dianpingoa.com/cat/r/p?date=2013111217&ip=All&step=-1&op=view&domain=MBookingWebShop&ip=All&urlThreshold=1000&sqlThreshold=100&serviceThreshold=50&cacheThreshold=10&callThreshold=50&Agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36";
		String agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36";

		Assert.assertEquals(agent, browserAnalyzer.parseValue("Agent", data));
		Assert.assertNull(browserAnalyzer.parseValue("UnknownKey", data));

	}
}
