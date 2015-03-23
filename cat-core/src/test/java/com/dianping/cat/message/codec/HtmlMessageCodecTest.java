package com.dianping.cat.message.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTrace;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
public class HtmlMessageCodecTest extends ComponentTestCase {

	private DefaultMessageTree buildMessageTree() {
		long timestamp = 1325489621987L;
		Transaction transaction = newTransaction("transaction", "transaction", timestamp, Message.SUCCESS, 8000, null);

		transaction.addChild(newTrace("logTrace", "<trace>", timestamp, Trace.SUCCESS, null));
		transaction.addChild(newTrace("logTrace", "<trace>", timestamp, Trace.SUCCESS, "data"));
		transaction.addChild(newEvent("logEvent", "<event>", timestamp, Trace.SUCCESS, "data"));
		transaction.addChild(newEvent("RemoteLink", "<event>", timestamp, Trace.SUCCESS, "data"));
		transaction.addChild(newMetric("logEvent", "<event>", timestamp, Trace.SUCCESS, "data"));
		transaction.addChild(newHeartbeat("logHeartbeat", "<event>", timestamp, Trace.SUCCESS, "data"));

		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setMessage(transaction);
		String messageId = "Cat-0a010680-384736-2061";
		String parentMessageId = "Cat-0a010680-384736-2062";

		tree.setHostName("localhost-cat");
		tree.setIpAddress("192.168.1.1");
		tree.setRootMessageId(messageId);
		tree.setParentMessageId(parentMessageId);

		for (Message message : transaction.getChildren()) {
			((AbstractMessage) message).setTimestamp(timestamp);
		}
		return tree;
	}

	private void check(MessageTree tree, Message message, String expected) throws Exception {
		HtmlMessageCodec codec = (HtmlMessageCodec) lookup(MessageCodec.class, "html");
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
		codec.encodeMessage(tree, message, buf, 0, null);
		String actual = buf.toString(Charset.forName("utf-8"));

		Assert.assertEquals(expected, actual);
	}

	private void checkTree(MessageTree tree, String expected) throws Exception {
		HtmlMessageCodec codec = (HtmlMessageCodec) lookup(MessageCodec.class, "html");
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
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

	private Metric newMetric(String type, String name, long timestamp, String status, String data) {
		DefaultMetric metric = new DefaultMetric(type, name);

		metric.setStatus(status);
		metric.addData(data);
		metric.setTimestamp(timestamp);
		return metric;
	}

	private Heartbeat newHeartbeat(String type, String name, long timestamp, String status, String data) {
		DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);

		heartbeat.setStatus(status);
		heartbeat.addData(data);
		heartbeat.setTimestamp(timestamp);
		return heartbeat;
	}

	private MessageTree newMessageTree() {
		MessageTree tree = new DefaultMessageTree();

		tree.setDomain("domain");
		tree.setHostName("hostName");
		tree.setIpAddress("ipAddress");
		tree.setMessageId("messageId");
		tree.setParentMessageId("parentMessageId");
		tree.setRootMessageId("rootMessageId");
		tree.setSessionToken("sessionToken");
		tree.setThreadGroupName("threadGroupName");
		tree.setThreadId("threadId");
		tree.setThreadName("threadName");

		return tree;
	}

	private Trace newTrace(String type, String name, long timestamp, String status, String data) {
		DefaultTrace trace = new DefaultTrace(type, name);

		trace.setStatus(status);
		trace.addData(data);
		trace.setTimestamp(timestamp);
		return trace;
	}

	private Transaction newTransaction(String type, String name, long timestamp, String status, int duration, String data) {
		DefaultTransaction transaction = new DefaultTransaction(type, name, null);

		transaction.setStatus(status);
		transaction.addData(data);
		transaction.complete();
		transaction.setTimestamp(timestamp);
		transaction.setDurationInMillis(duration);
		return transaction;
	}

	@Test
	public void testEncode() throws Exception {
		MessageTree tree = buildMessageTree();
		HtmlMessageCodec codec = (HtmlMessageCodec) lookup(MessageCodec.class, "html");
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
		codec.encode(tree, buf);

		String content = Files.forIO().readFrom(HtmlMessageCodecTest.class.getResourceAsStream("MessageTree.txt"),
		      "utf-8");
		Assert.assertEquals(content.trim(), tree.toString().trim());
		Assert.assertEquals(1308, buf.readableBytes());
	}

	@Test
	public void testEvent() throws Exception {
		long timestamp = 1325489621987L;
		Event event = newEvent("type", "name", timestamp, "0", "here is the data.");

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(event);
		check(tree, event,
		      "<tr><td>E15:33:41.987</td><td>type</td><td>name</td><td>&nbsp;</td><td>here is the data.</td></tr>\r\n");
	}

	@Test
	public void testMetric() throws Exception {
		long timestamp = 1325489621987L;
		Metric Metric = newMetric("type", "name", timestamp, "0", "here is the data.");

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(Metric);
		check(tree, Metric,
		      "<tr><td>M15:33:41.987</td><td>type</td><td>name</td><td>&nbsp;</td><td>here is the data.</td></tr>\r\n");
	}

	@Test
	public void testEventForRawData() throws Exception {
		long timestamp = 1325489621987L;
		String trace = "java.lang.Exception\n"
		      + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:112)\n"
		      + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:108)\n";
		Event event = newEvent("Exception", Exception.class.getName(), timestamp, "ERROR", trace);

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(event);
		check(tree,
		      event,
		      "<tr><td>E15:33:41.987</td><td>Exception</td><td>java.lang.Exception</td><td class=\"error\">ERROR</td><td>java.lang.Exception\n<br>"
		            + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:112)\n<br>"
		            + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:108)\n<br>"
		            + "</td></tr>\r\n");
	}

	@Test
	public void testHeartbeat() throws Exception {
		long timestamp = 1325489621987L;
		Heartbeat heartbeat = newHeartbeat("type", "name", timestamp, "0", "here is the data.");

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(heartbeat);
		check(tree, heartbeat,
		      "<tr><td>H15:33:41.987</td><td>type</td><td>name</td><td>&nbsp;</td><td>here is the data.</td></tr>\r\n");
	}

	@Test
	public void testMessageTree() throws Exception {
		MessageTree tree = newMessageTree();
		long timestamp = 1325489621987L;
		String expected1 = "<table class=\"logview\">\r\n"
		      + "<tr class=\"header\"><td colspan=5><a href=\'rootMessageId?domain=domain\'>RootLogview</a>&nbsp;&nbsp;"
		      + "<a href=\'parentMessageId?domain=domain\'>ParentLogview</a> HT2 domain hostName ipAddress threadGroupName threadId threadName messageId parentMessageId rootMessageId sessionToken </td></tr></table>";

		checkTree(tree, expected1);

		String expected2 = "<table class=\"logview\">\r\n"
		      + "<tr class=\"header\"><td colspan=5><a href=\'rootMessageId?domain=domain\'>RootLogview</a>&nbsp;&nbsp;<a href=\'parentMessageId?domain=domain\'>ParentLogview</a> HT2 domain hostName ipAddress threadGroupName threadId threadName messageId parentMessageId rootMessageId sessionToken </td></tr><tr class=\"odd\"><td>E15:33:41.987</td><td>type</td><td>name</td><td>&nbsp;</td><td>here is the data.</td></tr>"
		      + "\r\n</table>";

		tree.setMessage(newEvent("type", "name", timestamp, "0", "here is the data."));
		checkTree(tree, expected2);
	}

	@Test
	public void testTrace() throws Exception {
		long timestamp = 1325489621987L;
		Trace trace = newTrace("type", "name", timestamp, "0", "here is the data.");

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(trace);
		check(tree, trace,
		      "<tr><td>L15:33:41.987</td><td>type</td><td>name</td><td>&nbsp;</td><td>here is the data.</td></tr>\r\n");
	}

	@Test
	public void testTraceForRawData() throws Exception {
		long timestamp = 1325489621987L;
		String exception = "java.lang.Exception\n"
		      + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testTraceForException(PlainTextMessageCodecTest.java:112)\n"
		      + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testTraceForException(PlainTextMessageCodecTest.java:108)\n";
		Trace trace = newTrace("Exception", Exception.class.getName(), timestamp, "ERROR", exception);

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(trace);
		check(tree,
		      trace,
		      "<tr><td>L15:33:41.987</td><td>Exception</td><td>java.lang.Exception</td><td class=\"error\">ERROR</td><td>java.lang.Exception\n<br>"
		            + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testTraceForException(PlainTextMessageCodecTest.java:112)\n<br>"
		            + "\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testTraceForException(PlainTextMessageCodecTest.java:108)\n<br>"
		            + "</td></tr>\r\n");
	}

	@Test
	public void testTransactionNormal() throws Exception {
		long timestamp = 1325489621987L;
		Transaction root = newTransaction("URL", "Review", timestamp, "0", 100, "/review/2468");

		root.addChild(newEvent("URL", "Payload", timestamp, "0", "ip=127.0.0.1&ua=Mozilla 5.0...&refer=...&..."));
		root.addChild(newTransaction("Service", "Auth", timestamp, "0", 20, "userId=1357&token=..."));
		root.addChild(newTransaction("Cache", "findReviewByPK", timestamp + 22, "Missing", 1, "2468") //
		      .addChild(newEvent("CacheHost", "host-1", timestamp + 22, "0", "ip=192.168.8.123")));
		root.addChild(newTransaction("DAL", "findReviewByPK", timestamp + 25, "0", 5,
		      "select title,content from Review where id = ?"));
		root.addChild(newEvent("URL", "View", timestamp + 40, "0", "view=HTML"));

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(root);
		check(tree,
		      root,
		      "<tr><td>t15:33:41.987</td><td>URL</td><td>Review</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:41.987</td><td>URL</td><td>Payload</td><td>&nbsp;</td><td>ip=127.0.0.1&amp;ua=Mozilla 5.0...&amp;refer=...&amp;...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:41.987</td><td>Service</td><td>Auth</td><td>&nbsp;</td><td>20ms userId=1357&amp;token=...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;t15:33:42.009</td><td>Cache</td><td>findReviewByPK</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;E15:33:42.009</td><td>CacheHost</td><td>host-1</td><td>&nbsp;</td><td>ip=192.168.8.123</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;T15:33:42.010</td><td>Cache</td><td>findReviewByPK</td><td class=\"error\">Missing</td><td>1.00ms 2468</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:42.012</td><td>DAL</td><td>findReviewByPK</td><td>&nbsp;</td><td>5.00ms select title,content from Review where id = ?</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:42.027</td><td>URL</td><td>View</td><td>&nbsp;</td><td>view=HTML</td></tr>\r\n"
		            + "<tr><td>T15:33:42.087</td><td>URL</td><td>Review</td><td>&nbsp;</td><td>100ms /review/2468</td></tr>\r\n");
	}

	@Test
	public void testTransactionSimple() throws Exception {
		long timestamp = 1325489621987L;
		Transaction transaction = newTransaction("type", "name", timestamp, "0", 10, "here is the data.");

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(transaction);
		check(tree, transaction,
		      "<tr><td>A15:33:41.987</td><td>type</td><td>name</td><td>&nbsp;</td><td>10ms here is the data.</td></tr>\r\n");
	}

	@Test
	public void testTransactionWithRemoteCall() throws Exception {
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

		MessageTree tree = new DefaultMessageTree();
		tree.setMessage(root);
		tree.setMessageId("1637924287");
		check(tree,
		      root,
		      "<tr><td>t15:33:41.987</td><td>URL</td><td>Review</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:41.987</td><td>URL</td><td>Payload</td><td>&nbsp;</td><td>ip=127.0.0.1&amp;ua=Mozilla 5.0...&amp;refer=...&amp;...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:41.987</td><td>Service</td><td>Auth</td><td>&nbsp;</td><td>20ms userId=1357&amp;token=...</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;t15:33:42.009</td><td>Cache</td><td>findReviewByPK</td><td></td><td></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;E15:33:42.009</td><td>CacheHost</td><td>host-1</td><td>&nbsp;</td><td>ip=192.168.8.123</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;T15:33:42.010</td><td>Cache</td><td>findReviewByPK</td><td class=\"error\">Missing</td><td>1.00ms 2468</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:42.010</td><td>Service</td><td>ReviewService</td><td>&nbsp;</td><td>request data</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;<a href=\"/cat/r/m/domain1-c0a83f99-135bdb7825c-1\" onclick=\"return show(this,'domain1-c0a83f99-135bdb7825c-1');\">[:: show ::]</a></td><td colspan=\"4\"><div id=\"domain1-c0a83f99-135bdb7825c-1\"></div></td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;A15:33:42.012</td><td>DAL</td><td>findReviewByPK</td><td>&nbsp;</td><td>5.00ms select title,content from Review where id = ?</td></tr>\r\n"
		            + "<tr><td>&nbsp;&nbsp;E15:33:42.027</td><td>URL</td><td>View</td><td>&nbsp;</td><td>view=HTML</td></tr>\r\n"
		            + "<tr><td>T15:33:42.087</td><td>URL</td><td>Review</td><td>&nbsp;</td><td>100ms /review/2468</td></tr>\r\n");
	}
}
