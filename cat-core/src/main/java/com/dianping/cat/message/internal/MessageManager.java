package com.dianping.cat.message.internal;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.annotation.Inject;

public enum MessageManager {
	INSTANCE;

	private static final ThreadLocal<Context> s_context = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return new Context();
		}
	};

	@Inject
	private MessageSender m_sender;

	private boolean m_initialized;

	public void add(Message message) {
		s_context.get().add(this, message);
	}

	public void end(Transaction transaction) {
		s_context.get().end(this, transaction);
	}

	void flush(MessageTree tree) {
		if (m_sender != null) {
			m_sender.send(tree);
			
			// destroy current thread data
			s_context.remove();
		} else if (!m_initialized) {
			throw new IllegalStateException("MessageManager is not initialized yet!");
		}
	}

	public void initialize(MessageSender sender) {
		m_sender = sender;
		m_initialized = true;
	}

	public void start(Transaction transaction) {
		s_context.get().start(transaction);
	}

	static class Context {
		private DefaultMessageTree m_tree = new DefaultMessageTree();

		private Stack<Transaction> m_stack = new Stack<Transaction>();

		public void add(MessageManager manager, Message message) {
			if (m_stack.isEmpty()) {
				m_tree.setMessage(message);
			} else {
				Transaction entry = m_stack.peek();

				entry.addChild(message);
			}
		}

		public void end(MessageManager manager, Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction current = m_stack.peek();

				if (transaction.equals(current)) {
					validateTransaction(current);
				} else {
					throw new RuntimeException("Internal error: Transaction logging mismatched!");
				}

				m_stack.pop();

				if (m_stack.isEmpty()) {
					manager.flush(m_tree);
				}
			}
		}

		public void start(Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction entry = m_stack.peek();

				entry.addChild(transaction);
			}

			m_stack.push(transaction);
		}

		private void validateTransaction(Transaction transaction) {
			for (Message message : transaction.getChildren()) {
				if (message.getStatus() == null) {
					message.setStatus("unset");
				}

				if (!message.isCompleted() && message instanceof DefaultTransaction) {
					DefaultTransaction t = (DefaultTransaction) message;

					validateTransaction(t);

					// missing transaction end, log a BadInstrument event so that
					// developer can fix the code
					DefaultEvent notCompleteEvent = new DefaultEvent("CAT", "BadInstrument");

					notCompleteEvent.setStatus("TransactionNotCompleted");
					notCompleteEvent.setCompleted(true);
					transaction.addChild(notCompleteEvent);

					t.setCompleted(true);
				}
			}
		}
	}

	static class FakeTransaction extends AbstractMessage implements Transaction {
		public FakeTransaction() {
			super(null, null);
		}

		@Override
		public void addChild(Message message) {
		}

		@Override
		public void complete() {
		}

		@Override
		public List<Message> getChildren() {
			return Collections.emptyList();
		}

		@Override
		public long getDuration() {
			return -1;
		}
	}
}
