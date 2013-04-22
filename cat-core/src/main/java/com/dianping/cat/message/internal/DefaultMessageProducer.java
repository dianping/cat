package com.dianping.cat.message.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;

public class DefaultMessageProducer implements MessageProducer {
	@Inject
	private MessageManager m_manager;

	@Inject
	private MessageIdFactory m_factory;

	@Override
	public String createMessageId() {
		return m_factory.getNextId();
	}

	@Override
	public boolean isEnabled() {
		return m_manager.isCatEnabled();
	}

	@Override
	public void logError(Throwable cause) {
		StringWriter writer = new StringWriter(2048);

		cause.printStackTrace(new PrintWriter(writer));

		if (cause instanceof Error) {
			logEvent("Error", cause.getClass().getName(), "ERROR", writer.toString());
		} else if (cause instanceof RuntimeException) {
			logEvent("RuntimeException", cause.getClass().getName(), "ERROR", writer.toString());
		} else {
			logEvent("Exception", cause.getClass().getName(), "ERROR", writer.toString());
		}
	}

	@Override
	public void logEvent(String type, String name) {
		logEvent(type, name, Message.SUCCESS, null);
	}

	@Override
	public void logEvent(String type, String name, String status, String nameValuePairs) {
		Event event = newEvent(type, name);

		if (nameValuePairs != null && nameValuePairs.length() > 0) {
			event.addData(nameValuePairs);
		}

		event.setStatus(status);
		event.complete();
	}

	@Override
	public void logHeartbeat(String type, String name, String status, String nameValuePairs) {
		Heartbeat heartbeat = newHeartbeat(type, name);

		heartbeat.addData(nameValuePairs);
		heartbeat.setStatus(status);
		heartbeat.complete();
	}

	@Override
   public void logMetric(String type, String name, String status, String nameValuePairs) {
		Metric event = newMetric(type, name);

		if (nameValuePairs != null && nameValuePairs.length() > 0) {
			event.addData(nameValuePairs);
		}

		event.setStatus(status);
		event.complete();
   }

	@Override
	public Event newEvent(String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		if (m_manager.isCatEnabled()) {
			DefaultEvent event = new DefaultEvent(type, name);

			m_manager.add(event);
			return event;
		} else {
			return NullMessage.EVENT;
		}
	}
	
	public Event newEvent(Transaction parent, String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		if (m_manager.isCatEnabled() && parent != null) {
			DefaultEvent event = new DefaultEvent(type, name);

			parent.addChild(event);
			return event;
		} else {
			return NullMessage.EVENT;
		}
	}

	@Override
	public Heartbeat newHeartbeat(String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		if (m_manager.isCatEnabled()) {
			DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);

			m_manager.add(heartbeat);
			return heartbeat;
		} else {
			return NullMessage.HEARTBEAT;
		}
	}

	public Heartbeat newHeartbeat(Transaction parent, String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		if (m_manager.isCatEnabled() && parent != null) {
			DefaultHeartbeat heartbeat = new DefaultHeartbeat(type, name);

			parent.addChild(heartbeat);
			return heartbeat;
		} else {
			return NullMessage.HEARTBEAT;
		}
	}

	@Override
	public Metric newMetric(String type, String name) {
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}
		
		if (m_manager.isCatEnabled()) {
			DefaultMetric metric = new DefaultMetric(type, name);
			
			m_manager.add(metric);
			return metric;
		} else {
			return NullMessage.METRIC;
		}
	}

	@Override
	public Transaction newTransaction(String type, String name) {
		// this enable CAT client logging cat message without explicit setup
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		if (m_manager.isCatEnabled()) {
			DefaultTransaction transaction = new DefaultTransaction(type, name, m_manager);

			m_manager.start(transaction);
			return transaction;
		} else {
			return NullMessage.TRANSACTION;
		}
	}

	public Transaction newTransaction(Transaction parent, String type, String name) {
		// this enable CAT client logging cat message without explicit setup
		if (!m_manager.hasContext()) {
			m_manager.setup();
		}

		if (m_manager.isCatEnabled() && parent != null) {
			DefaultTransaction transaction = new DefaultTransaction(type, name, m_manager);

			parent.addChild(transaction);
			transaction.setStandalone(false);
			return transaction;
		} else {
			return NullMessage.TRANSACTION;
		}
	}
}
