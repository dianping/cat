package com.dianping.cat.message.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Transaction;
import com.site.lookup.annotation.Inject;

public class DefaultMessageProducer implements MessageProducer {
	@Inject
	private MessageManager m_manager;

	@Override
	public void logError(Throwable cause) {
		StringWriter writer = new StringWriter(2048);

		cause.printStackTrace(new PrintWriter(writer));

		logEvent("Error", cause.getClass().getName(), cause.getClass().getSimpleName(), writer.toString());
	}

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
		DefaultEvent event = new DefaultEvent(type, name);

		m_manager.add(event);
		return event;
	}

	@Override
	public Heartbeat newHeartbeat(String type, String name) {
		DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);

		m_manager.add(heartbeat);
		return heartbeat;
	}

	@Override
	public Transaction newTransaction(String type, String name) {
		DefaultTransaction transaction = new DefaultTransaction(type, name, m_manager);

		m_manager.start(transaction);
		return transaction;
	}
}
