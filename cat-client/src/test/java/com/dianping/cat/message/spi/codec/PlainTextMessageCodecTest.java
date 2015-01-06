package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.Charset;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTrace;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec.Context;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class PlainTextMessageCodecTest {
	private void check(Message message, String expected) {
		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10240);
		Context ctx = new Context().setBuffer(buf);

		codec.setBufferWriter(new EscapingBufferWriter());
		codec.encodeMessage(message, buf);

		String actual = buf.toString(Charset.forName("utf-8"));

		Assert.assertEquals(expected, actual);

		MessageTree tree = new DefaultMessageTree();

		codec.decodeMessage(ctx, tree);
		Assert.assertEquals(expected, tree.getMessage().toString());
	}

	private void checkTree(MessageTree tree, String expected) {
		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10240);
		codec.encode(tree, buf);
		buf.readInt(); // get rid of length

		String actual = buf.toString(Charset.forName("utf-8"));

		Assert.assertEquals(expected, actual);

		MessageTree t = codec.decode(buf);

		Assert.assertEquals(expected, t.toString());
	}

	private Event newEvent(String type, String name, long timestamp, String status, String data) {
		DefaultEvent event = new DefaultEvent(type, name);

		event.setStatus(status);
		event.addData(data);
		event.setTimestamp(timestamp);
		return event;
	}

	private Metric newMetric(String type, String name, long timestamp, String status, String data) {
		DefaultMetric Metric = new DefaultMetric(type, name);

		Metric.setStatus(status);
		Metric.addData(data);
		Metric.setTimestamp(timestamp);
		return Metric;
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
	public void testEvent() {
		long timestamp = 1325489621987L;
		Event event = newEvent("type", "name", timestamp, "0", "here is the data.");

		check(event, "E2012-01-02 15:33:41.987\ttype\tname\t0\there is the data.\t\n");
	}

	@Test
	public void testMetric() {
		long timestamp = 1325489621987L;
		Metric metric = newMetric("type", "name", timestamp, "0", "here is the data.");

		check(metric, "M2012-01-02 15:33:41.987\ttype\tname\t0\there is the data.\t\n");
	}

	@Test
	public void testEventForRawData() {
		long timestamp = 1325489621987L;
		String trace = "java.lang.Exception\n\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:112)\n";
		Event event = newEvent("Exception", Exception.class.getName(), timestamp, "ERROR", trace);

		check(event,
		      "E2012-01-02 15:33:41.987\tException\tjava.lang.Exception\tERROR\t" + //
		            "java.lang.Exception\\n\\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:112)\\n\t\n");
	}

	@Test
	public void testHeartbeat() {
		long timestamp = 1325489621987L;
		Heartbeat heartbeat = newHeartbeat("type", "name", timestamp, "0", "here is the data.");

		check(heartbeat, "H2012-01-02 15:33:41.987\ttype\tname\t0\there is the data.\t\n");
	}

	@Test
	public void testMessageTree() {
		MessageTree tree = newMessageTree();
		long timestamp = 1325489621987L;
		String expected = "PT1\tdomain\thostName\tipAddress\tthreadGroupName\tthreadId\tthreadName\tmessageId\tparentMessageId\trootMessageId\tsessionToken\n";

		checkTree(tree, expected);

		expected += "E2012-01-02 15:33:41.987\ttype\tname\t0\there is the data.\t\n";

		tree.setMessage(newEvent("type", "name", timestamp, "0", "here is the data."));
		checkTree(tree, expected);
	}

	@Test
	public void testTrace() {
		long timestamp = 1325489621987L;
		Trace trace = newTrace("type", "name", timestamp, "0", "here is the data.");

		check(trace, "L2012-01-02 15:33:41.987\ttype\tname\t0\there is the data.\t\n");
	}

	@Test
	public void testTraceForRawData() {
		long timestamp = 1325489621987L;
		String exception = "java.lang.Exception\n\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testTraceForException(PlainTextMessageCodecTest.java:112)\n";
		Trace trace = newTrace("Exception", Exception.class.getName(), timestamp, "ERROR", exception);

		check(trace,
		      "L2012-01-02 15:33:41.987\tException\tjava.lang.Exception\tERROR\t" + //
		            "java.lang.Exception\\n\\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testTraceForException(PlainTextMessageCodecTest.java:112)\\n\t\n");
	}

	@Test
	public void testTransactionNormal() {
		long timestamp = 1325489621987L;
		Transaction root = newTransaction("URL", "Review", timestamp, "0", 100, "/review/2468");

		root.addChild(newEvent("URL", "Payload", timestamp, "0", "ip=127.0.0.1&ua=Mozilla 5.0...&refer=...&..."));
		root.addChild(newTransaction("Service", "Auth", timestamp, "0", 20, "userId=1357&token=..."));
		root.addChild(newTransaction("Cache", "findReviewByPK", timestamp + 22, "Missing", 1, "2468") //
		      .addChild(newEvent("CacheHost", "host-1", timestamp + 22, "0", "ip=192.168.8.123")));
		root.addChild(newTransaction("DAL", "findReviewByPK", timestamp + 25, "0", 5, "select title,content from Review where id = ?"));
		root.addChild(newEvent("URL", "View", timestamp + 40, "0", "view=HTML"));

		check(root, "t2012-01-02 15:33:41.987\tURL\tReview\t\n" + //
		      "E2012-01-02 15:33:41.987\tURL\tPayload\t0\tip=127.0.0.1&ua=Mozilla 5.0...&refer=...&...\t\n" + //
		      "A2012-01-02 15:33:41.987\tService\tAuth\t0\t20000us\tuserId=1357&token=...\t\n" + //
		      "t2012-01-02 15:33:42.009\tCache\tfindReviewByPK\t\n" + //
		      "E2012-01-02 15:33:42.009\tCacheHost\thost-1\t0\tip=192.168.8.123\t\n" + //
		      "T2012-01-02 15:33:42.010\tCache\tfindReviewByPK\tMissing\t1000us\t2468\t\n" + //
		      "A2012-01-02 15:33:42.012\tDAL\tfindReviewByPK\t0\t5000us\tselect title,content from Review where id = ?\t\n" + //
		      "E2012-01-02 15:33:42.027\tURL\tView\t0\tview=HTML\t\n" + //
		      "T2012-01-02 15:33:42.087\tURL\tReview\t0\t100000us\t/review/2468\t\n");
	}

	@Test
	public void testTransactionSimple() {
		long timestamp = 1325489621987L;
		Transaction transaction = newTransaction("type", "name", timestamp, "0", 10, "here is the data.");

		check(transaction, "A2012-01-02 15:33:41.987\ttype\tname\t0\t10000us\there is the data.\t\n");
	}

}