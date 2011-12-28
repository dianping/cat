package com.dianping.cat.message.internal;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public enum MessageManager {
	INSTANCE;

	private static final ThreadLocal<Context> s_context = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return new Context();
		}
	};

	public void add(Message message) {
		s_context.get().add(message);
	}

	public void end(Transaction transaction) {
		s_context.get().end(transaction);
	}

	public void start(Transaction transaction) {
		s_context.get().start(transaction);
	}

	void handle(Transaction transaction) {
		// TODO
		System.out.println(transaction);
	}

	static class Context {
		private Stack<Transaction> m_stack = new Stack<Transaction>();

		public void add(Message message) {
			if (!m_stack.isEmpty()) {
				Transaction entry = m_stack.peek();

				entry.addChild(message);
			} else {
				// add a mock transaction as its parent
				Transaction t = new FakeTransaction();

				start(t);
				t.addChild(message);
				end(t);
			}
		}

		public void end(Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction current = m_stack.peek();

				if (transaction.equals(current)) {
					validateTransaction(current);
				} else {
					throw new RuntimeException("Internal error: Transaction logging mismatched!");
				}

				m_stack.pop();

				if (m_stack.isEmpty()) {
					INSTANCE.handle(transaction);
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
		public void complete() {
		}

		@Override
		public long getDuration() {
			return -1;
		}

		@Override
		public List<Message> getChildren() {
			return Collections.emptyList();
		}

		@Override
		public void addChild(Message message) {
		}
	}
}
