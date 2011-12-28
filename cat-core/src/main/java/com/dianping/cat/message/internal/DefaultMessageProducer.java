package com.dianping.cat.message.internal;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;

public class DefaultMessageProducer implements MessageProducer {
	@Override
	public void logEvent(String type, String name, String status, String nameValuePairs) {
		Event event = newEvent(type, name);

		event.addData(nameValuePairs);
		event.setStatus(Message.SUCCESS);
		event.complete();
	}

	@Override
	public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
		Heartbeat heartbeat = newHeartbeat(type, name);

		heartbeat.addData(nameValuePairs);
		heartbeat.setStatus(Message.SUCCESS);
		heartbeat.complete();
	}

	@Override
	public Event newEvent(String type, String name) {
		return new DefaultEvent(type, name);
	}

	@Override
	public Heartbeat newHeartbeat(String type, String name) {
		return new DefaultHeartbeat(type, name);
	}

	@Override
	public Transaction newTransaction(String type, String name) {
		return new DefaultTransaction(type, name);
	}
}
