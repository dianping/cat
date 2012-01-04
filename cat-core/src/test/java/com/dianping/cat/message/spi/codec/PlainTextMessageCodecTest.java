package com.dianping.cat.message.spi.codec;

import java.nio.charset.Charset;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class PlainTextMessageCodecTest {
	private void check(Message message, String expected) {
		PlainTextMessageCodec codec = new PlainTextMessageCodec();
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		codec.encodeMessage(message, buf);
		String actual = buf.toString(Charset.forName("utf-8"));

		Assert.assertEquals(expected, actual);

		MessageTree tree = new DefaultMessageTree();

		codec.decodeMessage(buf, tree);

		Assert.assertEquals(expected, tree.getMessage().toString());
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
		tree.setPort(1234);
		tree.setRequestToken("requestToken");
		tree.setSessionToken("sessionToken");
		tree.setThreadId("threadId");

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

		check(event, "E2012-01-02 15:33:41.987\ttype\tname\t0\there is the data.\t\n");
	}

	@Test
	public void testEventForRawData() {
		long timestamp = 1325489621987L;

		Event event = newEvent(
		      "Exception",
		      Exception.class.getName(),
		      timestamp,
		      "ERROR",
		      "java.lang.Exception\n\tat com.dianping.cat.message.spi.codec.PlainTextMessageCodecTest.testEventForException(PlainTextMessageCodecTest.java:112)\n");// toString(e)

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
		DefaultMessageTree tree = newMessageTree();
		long timestamp = 1325489621987L;

		Assert.assertEquals("PT1\tdomain\thostName\t1234\tipAddress\tthreadId\tmessageId\trequestToken\tsessionToken\n",
		      tree.toString());

		tree.setMessage(newEvent("type", "name", timestamp, "0", "here is the data."));

		Assert.assertEquals("PT1\tdomain\thostName\t1234\tipAddress\tthreadId\tmessageId\trequestToken\tsessionToken\n" + //
		      "E2012-01-02 15:33:41.987\ttype\tname\t0\there is the data.\t\n", tree.toString());
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

		check(root, "t2012-01-02 15:33:41.987\tURL\tReview\t\n" + //
		      "E2012-01-02 15:33:41.987\tURL\tPayload\t0\tip=127.0.0.1&ua=Mozilla 5.0...&refer=...&...\t\n" + //
		      "A2012-01-02 15:33:41.987\tService\tAuth\t0\t20ms\tuserId=1357&token=...\t\n" + //
		      "t2012-01-02 15:33:42.009\tCache\tfindReviewByPK\t\n" + //
		      "E2012-01-02 15:33:42.009\tCacheHost\thost-1\t0\tip=192.168.8.123\t\n" + //
		      "T2012-01-02 15:33:42.010\tCache\tfindReviewByPK\tMissing\t1ms\t2468\t\n" + //
		      "A2012-01-02 15:33:42.012\tDAL\tfindReviewByPK\t0\t5ms\tselect title,content from Review where id = ?\t\n" + //
		      "E2012-01-02 15:33:42.027\tURL\tView\t0\tview=HTML\t\n" + //
		      "T2012-01-02 15:33:42.087\tURL\tReview\t0\t100ms\t/review/2468\t\n");
	}

	@Test
	public void testTransactionSimple() {
		long timestamp = 1325489621987L;
		Transaction transaction = newTransaction("type", "name", timestamp, "0", 10, "here is the data.");

		check(transaction, "A2012-01-02 15:33:41.987\ttype\tname\t0\t10ms\there is the data.\t\n");
	}
}
