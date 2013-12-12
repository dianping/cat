package com.dianping.cat.consumer.performance;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.browser.BrowserAnalyzer;
import com.dianping.cat.consumer.browser.UserAgentParser;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class BrowserPerformanceTest extends ComponentTestCase {

	public void testStandardIE6() {
		String header = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; T312461; .NET CLR 1.1.4322)";
		UserAgentParser uap = new UserAgentParser(header);
		assertEquals("Windows NT 5.0", uap.getBrowserOperatingSystem());
		assertEquals("MSIE", uap.getBrowserName());
		assertEquals("6.0", uap.getBrowserVersion());
	}

	// @Test
	// public void performanceTest(){
	// long current = System.currentTimeMillis();
	//
	// int size = 10000000;
	// for (int i = 0; i < size; i++) {
	// testStandardIE6();
	// }
	// System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
	// }
	// //Cost 74

	@Test
	public void test() throws Exception {
		BrowserAnalyzer analyzer = (BrowserAnalyzer) lookup(MessageAnalyzer.class, BrowserAnalyzer.ID);
		MessageTree tree = buildMessage();
		long current = System.currentTimeMillis();
		analyzer.initialize(new Date().getTime(), Constants.HOUR, Constants.MINUTE * 5);

		int size = 100000;
		for (int i = 0; i < size; i++) {
			analyzer.process(tree);
		}
		System.out.println(analyzer.getReport("Cat"));
		System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
		// cost 26
	}
	
	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("URL", "/redirect", 112819)
				      .child(
				            e("URL",
				                  "URL.Server",
				                  "RemoteIP=10.1.1.109&VirtualIP=10.1.1.109&Server=cat.dianpingoa.com&Referer=http://cat.dianpingoa.com/cat/r/p?date=2013111217&ip=All&step=-1&op=view&domain=MBookingWebShop&ip=All&urlThreshold=1000&sqlThreshold=100&serviceThreshold=50&cacheThreshold=10&callThreshold=50&Agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36"))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft1(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft2(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft3(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")))
				      .child(
				            t("PigeonCall",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft4(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonCall.server", "10.1.2.199:2011", "Execute[34796272]")))
				      .child(
				            t("PigeonService",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft5(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonService.client", "10.1.7.127:37897", "Execute[34796272]")))
				      .child(
				            t("PigeonService",
				                  "groupService:groupNoteService_1.0.1:updateNoteDraft7(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonService.client", "10.1.7.127:37897", "Execute[34796272]")))
				      .child(
				            t("PigeonService",
				                  "groupService:groupNoteService_1.0.1:updateNoteD1aft6(Integer,Integer,String,String)",
				                  "", 100).child(e("PigeonService1.client", "10.1.7.128:37897", "Execute[34796272]")));

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("Cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		tree.setMessageId("MobileApi-0a01077f-379304-1362256");
		return tree;
	}

}
