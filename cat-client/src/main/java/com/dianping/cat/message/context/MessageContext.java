package com.dianping.cat.message.context;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;

public interface MessageContext {
	void add(Message message);

	void attach(ForkedTransaction child);

	void detach(String rootMessageId, String parentMessageId);

	void end(Transaction transaction);

	MessageTree getMessageTree();

	MessageTree getMessageTreeWithMessageId();

	boolean hasException(Throwable e);

	boolean hasPeekTransaction();

	Event newEvent(String type, String name);

	Event newEvent(String message, Throwable cause);

	Heartbeat newHeartbeat(String type, String name);

	Trace newTrace(String type, String name);

	Transaction newTransaction(String type, String name);

	String nextMessageId();

	Transaction peekTransaction();

	void start(Transaction transaction);
}
