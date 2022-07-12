package com.dianping.cat.message.context;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultMessageTree;
import com.dianping.cat.message.internal.DefaultTrace;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.pipeline.MessagePipeline;

public class DefaultTraceContext implements TraceContext {
	private MessagePipeline m_pipeline;

	private DefaultMessageTree m_tree;

	private Stack<Transaction> m_stack = new Stack<Transaction>();

	private Set<Integer> m_exceptions = new HashSet<Integer>();

	DefaultTraceContext(MessagePipeline pipeline, MessageIdFactory factory) {
		m_pipeline = pipeline;
		m_tree = new DefaultMessageTree(factory);
	}

	@Override
	public void add(Message message) {
		if (m_stack.isEmpty()) {
			m_tree.setMessage(message);
			deliver(m_tree);
		} else {
			m_stack.peek().addChild(message);
		}
	}

	@Override
	public void attach(ForkedTransaction forked) {
		m_stack.push(forked);
	}

	private void deliver(DefaultMessageTree tree) {
		m_pipeline.headContext(tree).fireMessage(tree);
		tree.reset();
	}

	@Override
	public void detach(String rootMessageId, String parentMessageId) {
		Transaction peek = m_stack.pop();

		if (parentMessageId != null && peek instanceof ForkedTransaction) {
			ForkedTransaction forked = (ForkedTransaction) peek;
			DefaultMessageTree tree = m_tree.copy();

			if (rootMessageId == null) {
				rootMessageId = parentMessageId;
			}

			tree.setRootMessageId(rootMessageId);
			tree.setParentMessageId(parentMessageId);
			tree.setMessageId(forked.getMessageId());
			tree.setMessage(forked);

			deliver(tree);
			m_tree.reset();
		}
	}

	@Override
	public void end(Transaction transaction) {
		Transaction child = m_stack.pop();

		// in case of child transactions are not completed explicitly
		while (transaction != child && !m_stack.isEmpty()) {
			Transaction parent = m_stack.pop();

			child = parent;
		}

		if (m_stack.isEmpty()) {
			deliver(m_tree);
		}
	}

	@Override
	public MessageTree getMessageTree() {
		return m_tree;
	}

	@Override
	public boolean hasException(Throwable e) {
		int hash = System.identityHashCode(e);

		if (m_exceptions.contains(hash)) {
			return true;
		} else {
			m_exceptions.add(hash);
			return false;
		}
	}

	@Override
	public boolean hasPeekTransaction() {
		return !m_stack.isEmpty();
	}

	@Override
	public Event newEvent(String type, String name) {
		return new DefaultEvent(this, type, name);
	}

	@Override
	public Event newEvent(String message, Throwable cause) {
		return new DefaultEvent(this, message, cause);
	}

	@Override
	public Heartbeat newHeartbeat(String type, String name) {
		return new DefaultHeartbeat(this, type, name);
	}

	@Override
	public Trace newTrace(String type, String name) {
		return new DefaultTrace(this, type, name);
	}

	@Override
	public Transaction newTransaction(String type, String name) {
		return new DefaultTransaction(this, type, name);
	}

	@Override
	public Transaction peekTransaction() {
		if (m_stack.isEmpty()) {
			throw new RuntimeException("Stack is empty!");
		} else {
			return m_stack.peek();
		}
	}

	@Override
	public void start(Transaction transaction) {
		if (m_stack.isEmpty()) {
			m_tree.setMessage(transaction);
		} else {
			m_stack.peek().addChild(transaction);
		}

		m_stack.push(transaction);
	}
}
