package com.dianping.cat.message.internal;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.internal.NullMessageTree;

public class NullMessageProducer implements MessageProducer {
	public static final NullMessageProducer NULL_MESSAGE_PRODUCER = new NullMessageProducer();

	private AtomicInteger seq = new AtomicInteger(0);

	@Override
	public String createRpcServerId(String domain) {
		return createMessageId();
	}

	@Override
	public String createMessageId() {
		return NullMessageTree.UNKNOWN + "-00000000-000000-" + seq.getAndIncrement();
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void logError(String message, Throwable cause) {

	}

	@Override
	public void logError(Throwable cause) {

	}

	@Override
	public void logEvent(String type, String name) {

	}

	@Override
	public void logEvent(String type, String name, String status, String nameValuePairs) {

	}

	@Override
	public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
	}

	@Override
	public void logMetric(String name, String status, String nameValuePairs) {
	}

	@Override
	public void logTrace(String type, String name) {

	}

	@Override
	public void logTrace(String type, String name, String status, String nameValuePairs) {
	}

	@Override
	public Event newEvent(String type, String name) {
		return NullMessage.EVENT;
	}

	@Override
	public ForkedTransaction newForkedTransaction(String type, String name) {
		return NullMessage.TRANSACTION;
	}

	@Override
	public Heartbeat newHeartbeat(String type, String name) {
		return NullMessage.HEARTBEAT;
	}

	@Override
	public Metric newMetric(String type, String name) {
		return NullMessage.METRIC;
	}

	@Override
	public TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
		return NullMessage.TRANSACTION;
	}

	@Override
	public Trace newTrace(String type, String name) {
		return NullMessage.TRACE;
	}

	@Override
	public Transaction newTransaction(String type, String name) {
		return NullMessage.TRANSACTION;
	}

}
