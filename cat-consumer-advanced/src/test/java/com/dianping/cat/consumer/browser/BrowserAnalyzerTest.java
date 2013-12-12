package com.dianping.cat.consumer.browser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class BrowserAnalyzerTest extends ComponentTestCase {

	private long m_timestamp;

	private BrowserAnalyzer m_analyzer;

	private String m_domain = "group";

	@Test
	public void testParseValue() {
		BrowserAnalyzer browserAnalyzer = new BrowserAnalyzer();
		String data = "RemoteIP=10.1.1.109&VirtualIP=10.1.1.109&Server=cat.dianpingoa.com&Referer=http://cat.dianpingoa.com/cat/r/p?date=2013111217&ip=All&step=-1&op=view&domain=MBookingWebShop&ip=All&urlThreshold=1000&sqlThreshold=100&serviceThreshold=50&cacheThreshold=10&callThreshold=50&Agent=Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36";
		String agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36";

		Assert.assertEquals(agent, browserAnalyzer.parseValue("Agent", data));
		Assert.assertNull(browserAnalyzer.parseValue("UnknownKey", data));

	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);

		m_analyzer = (BrowserAnalyzer) lookup(MessageAnalyzer.class, BrowserAnalyzer.ID);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 100; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		BrowserReport report = m_analyzer.getReport("Cat");

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("browser_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t;

		if (i % 2 == 0) {
			t = new DefaultTransaction("URL", "Cat-Test-Call", null);
			DefaultEvent event = new DefaultEvent("URL", "URL.Server");
			event.addData("RemoteIP=221.226.186.58&VirtualIP=127.0.0.1&Server=t.dianping.com&Referer=http://youku.com.gambol.pw/abc.flv&Agent=Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");

			event.setTimestamp(m_timestamp + 5 * 60 * 1000);
			event.setStatus(Message.SUCCESS);
			t.addChild(event);
		} else {
			t = new DefaultTransaction("URL", "Cat-Test-Service", null);
			DefaultEvent event = new DefaultEvent("URL", "ClientInfo");
			event.addData("RemoteIP=111.172.157.237&VirtualIP=127.0.0.1&Server=t.dianping.com&Referer=http://t.dianping.com/wuhan?utm_source=sogou_tglogo&Agent=Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.802.30 Safari/535.1 SE 2.X MetaSr 1.0");
			event.setTimestamp(m_timestamp + 5 * 60 * 1000);
			event.setStatus(Message.SUCCESS);
			t.addChild(event);
		}

		t.complete();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		tree.setMessage(t);

		return tree;
	}
}
