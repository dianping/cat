package com.dianping.cat.message.context;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultHeartbeat;
import com.dianping.cat.message.internal.DefaultTrace;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.tree.DefaultMessageTree;
import com.dianping.cat.message.tree.MessageTree;

public class DefaultMessageContext implements MessageContext {
	private DefaultMessageTree m_tree = new DefaultMessageTree();

	private Stack<Transaction> m_stack = new Stack<Transaction>();

	private Set<Integer> m_exceptions = new HashSet<Integer>();

	@Override
	public void add(Message message) {
		if (m_stack.isEmpty()) {
			m_tree.setMessage(message);
		} else {
			m_stack.peek().addChild(message);
		}
	}

	@Override
	public void end(Transaction transaction) {
		m_stack.pop();
		
		if (m_stack.isEmpty()) {
			deliverMessage(m_tree);
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
	public void start(Transaction transaction) {
		if (m_stack.isEmpty()) {
			m_tree.setMessage(transaction);
		} else {
			m_stack.peek().addChild(transaction);
		}

		m_stack.push(transaction);
	}

	@Override
	public void attach(ForkedTransaction forked) {
		m_stack.push(forked);
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

			deliverMessage(tree);
			m_tree.reset();
		}
	}

	private void deliverMessage(MessageTree tree) {
		// TODO
	}
}
