package com.dianping.cat.report.service;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import org.unidal.lookup.ContainerHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ModelServiceWithCalSupport extends ContainerHolder {
	private Transaction m_current;

	protected void logError(Throwable cause) {
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

	protected void logEvent(String type, String name, String status, String nameValuePairs) {
		DefaultEvent event = new DefaultEvent(type, name);

		m_current.addChild(event);

		if (nameValuePairs != null && nameValuePairs.length() > 0) {
			event.addData(nameValuePairs);
		}
		event.setStatus(status);
		event.complete();
	}

	protected Transaction newTransaction(String type, String name) {
		DefaultMessageProducer cat = (DefaultMessageProducer) Cat.getProducer();

		return cat.newTransaction(m_current, type, name);
	}

	protected void setParentTransaction(Transaction current) {
		m_current = current;
	}
}
