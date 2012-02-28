package com.dianping.cat.message.spi.codec;

import java.nio.charset.Charset;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Ignore;
import org.junit.Test;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessagePathBuilder;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class HtmlMessageCodecTest {
	private void check(Message message, String expected) {
		HtmlMessageCodec codec = new HtmlMessageCodec();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		codec.setBufferWriter(new HtmlEncodingBufferWriter());
		codec.setMessagePathBuilder(new DefaultMessagePathBuilder());
		codec.encodeMessage(message, buf, 0, null);
		String actual = buf.toString(Charset.forName("utf-8"));

		Assert.assertEquals(expected, actual);
	}

	private void checkTree(MessageTree tree, String expected) {
		HtmlMessageCodec codec = new HtmlMessageCodec();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		codec.setBufferWriter(new HtmlEncodingBufferWriter());
		codec.setMessagePathBuilder(new DefaultMessagePathBuilder());
		codec.encode(tree, buf);
		buf.readInt(); // get rid of length
		String actual = buf.toString(Charset.forName("utf-8"));

		Assert.assertEquals(expected, actual);
	}

	private Event newEvent(String type, String name, long timestamp, String status, String data) {
		DefaultEvent event = new DefaultEvent(type, name);

		event.setStatus(status);
		event.addData(data);
		event.setTimestamp(timestamp);
		return event;
	}

	private Heartbeat newHeartbeat(String type, String name, long timestamp, String status, String data) {
		DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);

		heartbeat.setStatus(status);
		heartbeat.addData(data);
		heartbeat.setTimestamp(timestamp);
		return heartbeat;
	}

	private DefaultMessageTree newMessageTree() {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain("domain");
		tree.setHostName("hostName");
		tree.setIpAddress("ipAddress");
		tree.setMessageId("messageId");
		tree.setParentMessageId("parentMessageId");
		tree.setRootMessageId("rootMessageId");
		tree.setSessionToken("sessionToken");
		tree.setThreadId("threadId");
		tree.setThreadName("threadName");

		return tree;
	}

	private Transaction newTransaction(String type, String name, long timestamp, String status, int duration, String data) {
		DefaultTransaction transaction = new DefaultTransaction(type, name, null);

		transaction.setStatus(status);
		transaction.addData(data);
		transaction.complete();
		transaction.setTimestamp(timestamp);
		transaction.setDuration(duration);
		return transaction;
	}

	@Test
	public void testEvent() {
		long timestamp = 1325489621987L;
		Event event = newEvent("type", "name", timestamp, "0", "here is the data.");

		check(event, "<tr><td>E15:33:41.987</td><td>type</td><td>name</td><td>0</td><td>here is the data.</td></tr>\r\n");
	}

	@Test
	public void testEventForRawData() {
		long timestamp = 1325489621987L;
		String trace = "java.lang.Exception\n"
		      + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:112)\n"
		      + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:108)\n";
		Event event = newEvent("Exception", Exception.class.getName(), timestamp, "ERROR", trace);

		check(event,
		      "<tr><td>E15:33:41.987</td><td>Exception</td><td>java.lang.Exception</td><td class=\"error\">ERROR</td><td>java.lang.Exception\n<br>"
		            + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:112)\n<br>"
		            + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:108)\n<br>"
		            + "</td></tr>\r\n");
	}

	@Test
	public void testHeartbeat() {
		long timestamp = 1325489621987L;
		Heartbeat heartbeat = newHeartbeat("type", "name", timestamp, "0", "here is the data.");

		check(heartbeat,
		      "<tr><td>H15:33:41.987</td><td>type</td><td>name</td><td>0</td><td>here is the data.</td></tr>\r\n");
	}

	@Test
	@Ignore
	public void testMessageTree() {
		DefaultMessageTree tree = newMessageTree();
		long timestamp = 1325489621987L;
		String expected1 = "<table class=\"logview\">\r\n"
		      + "<tr><td>HT1</td><td>domain</td><td>hostName</td><td>ipAddress</td><td>threadId</td><td>threadName</td><td>messageId</td><td>requestToken</td><td>sessionToken</td></tr>\r\n"
		      + "</table>";

		checkTree(tree, expected1);

		String expected2 = "<table class=\"logview\">\r\n"
		      + "<tr><td>HT1</td><td>domain</td><td>hostName</td><td>ipAddress</td><td>threadId</td><td>messageId</td><td>requestToken</td><td>sessionToken</td></tr>\r\n"
		      + "<tr class=\"odd\"><td>E15:33:41.987</td><td>type</td><td>name</td><td>0</td><td>here is the data.</td></tr>\r\n"
		      + "</table>";

		tree.setMessage(newEvent("type", "name", timestamp, "0", "here is the data."));
		checkTree(tree, expected2);
	}

	@Test
	public void testTransactionNormal() {
		long timestamp = 1325489621987L;
		Transaction root = newTransaction("URL", "Review", timestamp, "0", 100, "/review/2468");

		root.addChild(newEvent("URL", "Payload", timestamp, "0", "ip=127.0.0.1&ua=Mozilla 5.0...&refer=...&..."));
		root.addChild(newTransaction("Service", "Auth", timestamp, "0", 20, "userId=1357&token=..."));
		root.addChild(newTransaction("Cache", "findReviewByPK", timestamp + 22, "Missing", 1, "2468") //
		      .addChild(newEvent("CacheHost", "host-1", timestamp + 22, "0", "ip=192.168.8.123")));
		root.addChild(newTransaction("DAL", "findReviewByPK", timestamp + 25, "0", 5,
		      "select title,content from Review where id = ?"));
		root.addChild(newEvent("URL", "View", timestamp + 40, "0", "view=HTML"));

		check(root,
		      "<tr><td>t15:33:41.987</td><td>URL</td><td>Review</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:41.987</td><td>URL</td><td>Payload</td><td>0</td><td>ip=127.0.0.1&amp;ua=Mozilla 5.0...&amp;refer=...&amp;...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:41.987</td><td>Service</td><td>Auth</td><td>0</td><td>20ms userId=1357&amp;token=...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;t15:33:42.009</td><td>Cache</td><td>findReviewByPK</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;E15:33:42.009</td><td>CacheHost</td><td>host-1</td><td>0</td><td>ip=192.168.8.123</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;T15:33:42.010</td><td>Cache</td><td>findReviewByPK</td><td class=\"error\">Missing</td><td>1ms 2468</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:42.012</td><td>DAL</td><td>findReviewByPK</td><td>0</td><td>5ms select title,content from Review where id = ?</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:42.027</td><td>URL</td><td>View</td><td>0</td><td>view=HTML</td></tr>\r\n"
		            + "<tr><td>T15:33:42.087</td><td>URL</td><td>Review</td><td>0</td><td>100ms /review/2468</td></tr>\r\n");
	}

	@Test
	public void testTransactionWithRemoteCall() {
		long timestamp = 1325489621987L;
		Transaction root = newTransaction("URL", "Review", timestamp, "0", 100, "/review/2468");

		root.addChild(newEvent("URL", "Payload", timestamp, "0", "ip=127.0.0.1&ua=Mozilla 5.0...&refer=...&..."));
		root.addChild(newTransaction("Service", "Auth", timestamp, "0", 20, "userId=1357&token=..."));
		root.addChild(newTransaction("Cache", "findReviewByPK", timestamp + 22, "Missing", 1, "2468") //
		      .addChild(newEvent("CacheHost", "host-1", timestamp + 22, "0", "ip=192.168.8.123")));
		root.addChild(newEvent("Service", "ReviewService", timestamp + 23, "0", "request data"));
		root.addChild(newEvent("RemoteCall", "Pigeon", timestamp + 23, "0", "domain1-c0a83f99-135bdb7825c-1"));
		root.addChild(newTransaction("DAL", "findReviewByPK", timestamp + 25, "0", 5,
		      "select title,content from Review where id = ?"));
		root.addChild(newEvent("URL", "View", timestamp + 40, "0", "view=HTML"));

		check(root,
		      "<tr><td>t15:33:41.987</td><td>URL</td><td>Review</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:41.987</td><td>URL</td><td>Payload</td><td>0</td><td>ip=127.0.0.1&amp;ua=Mozilla 5.0...&amp;refer=...&amp;...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:41.987</td><td>Service</td><td>Auth</td><td>0</td><td>20ms userId=1357&amp;token=...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;t15:33:42.009</td><td>Cache</td><td>findReviewByPK</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;E15:33:42.009</td><td>CacheHost</td><td>host-1</td><td>0</td><td>ip=192.168.8.123</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;T15:33:42.010</td><td>Cache</td><td>findReviewByPK</td><td class=\"error\">Missing</td><td>1ms 2468</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:42.010</td><td>Service</td><td>ReviewService</td><td>0</td><td>request data</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;<a href=\"/cat/m/20120227/15/domain1/domain1-c0a83f99-135bdb7825c-1.html\" onclick=\"show(this,1677274581);return false;\">[:: show ::]</a></td><td colspan=\"4\"><div id=\"1677274581\"></div></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:42.012</td><td>DAL</td><td>findReviewByPK</td><td>0</td><td>5ms select title,content from Review where id = ?</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:42.027</td><td>URL</td><td>View</td><td>0</td><td>view=HTML</td></tr>\r\n"
		            + "<tr><td>T15:33:42.087</td><td>URL</td><td>Review</td><td>0</td><td>100ms /review/2468</td></tr>\r\n");
	}

	@Test
	public void testTransactionSimple() {
		long timestamp = 1325489621987L;
		Transaction transaction = newTransaction("type", "name", timestamp, "0", 10, "here is the data.");

		check(transaction,
		      "<tr><td>A15:33:41.987</td><td>type</td><td>name</td><td>0</td><td>10ms here is the data.</td></tr>\r\n");
	}
}
