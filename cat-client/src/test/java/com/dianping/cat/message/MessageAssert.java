package com.dianping.cat.message;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.junit.Assert;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.message.internal.DefaultMessageTree;
import com.dianping.cat.message.pipeline.MessageHandler;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

public class MessageAssert {
	private static Stack<MessageTree> s_trees = new Stack<MessageTree>();

	public static EventAssert event() {
		MessageTree tree = s_trees.peek();
		Message message = tree.getMessage();

		if (message == null) {
			Assert.fail("No message found!");
		} else if (!(message instanceof Event)) {
			Assert.fail("No event found, but was " + message.getClass().getName());
		}

		return new EventAssert((Event) message);
	}

	public static EventAssert eventBy(String type) {
		List<String> types = new ArrayList<String>();

		for (MessageTree tree : new ArrayList<MessageTree>(s_trees)) {
			Message message = tree.getMessage();

			if (message instanceof Event) {
				if (message.getType().equals(type)) {
					return new EventAssert((Event) message);
				} else if (!types.contains(message.getType())) {
					types.add(message.getType());
				}
			}
		}

		Assert.fail(String.format("No event(%s) found, but was %s!", type, types.toString()));

		return null; // this will NEVER be reached
	}

	public static HeaderAssert header() {
		MessageTree tree = s_trees.peek();

		return new HeaderAssert(tree);
	}

	public static HeaderAssert headerByTransaction(String type) {
		List<String> types = new ArrayList<String>();

		for (MessageTree tree : new ArrayList<MessageTree>(s_trees)) {
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				if (message.getType().equals(type)) {
					return new HeaderAssert(tree);
				} else if (!types.contains(message.getType())) {
					types.add(message.getType());
				}
			}
		}

		Assert.fail(String.format("No message tree(%s) found, but was %s!", type, types.toString()));

		return null; // this will NEVER be reached
	}

	public static HeartbeatAssert heartbeat() {
		MessageTree tree = s_trees.peek();
		Message message = tree.getMessage();

		if (message == null) {
			Assert.fail("No message found!");
		} else if (!(message instanceof Heartbeat)) {
			Assert.fail("No heartbeat found, but was " + message.getClass().getName());
		}

		return new HeartbeatAssert((Heartbeat) message);
	}

	public static void intercept(ComponentContext ctx) {
		ctx.registerComponent(MessageHandler.class, new MessageInterceptor());
	}

	private static void newTree(MessageTree tree) {
		if (tree instanceof DefaultMessageTree) {
			s_trees.push(((DefaultMessageTree) tree).copy());
		} else {
			throw new IllegalStateException("Unknown message tree implementation: " + tree.getClass());
		}
	}

	public static void reset() {
		s_trees.clear();
	}

	public static TransactionAssert transaction() {
		MessageTree tree = s_trees.peek();
		Message message = tree.getMessage();

		if (message == null) {
			Assert.fail("No message found!");
		} else if (!(message instanceof Transaction)) {
			Assert.fail("No transaction found, but was " + message.getClass().getName());
		}

		return new TransactionAssert((Transaction) message);
	}

	public static TransactionAssert transactionBy(String type) {
		List<String> types = new ArrayList<String>();

		for (MessageTree tree : new ArrayList<MessageTree>(s_trees)) {
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				if (message.getType().equals(type)) {
					return new TransactionAssert((Transaction) message);
				} else if (!types.contains(message.getType())) {
					types.add(message.getType());
				}
			}
		}

		Assert.fail(String.format("No transaction(%s) found, but was %s!", type, types.toString()));

		return null; // this will NEVER be reached
	}

	public static MessageTreeAssert tree(String messageId) {
		List<String> messageIds = new ArrayList<String>();

		for (MessageTree tree : new ArrayList<MessageTree>(s_trees)) {
			messageIds.add(tree.getMessageId());

			if (tree.getMessageId().equals(messageId)) {
				return new MessageTreeAssert(tree);
			}
		}

		Assert.fail(String.format("No message tree(%s) found, but was %s!", messageId, messageIds.toString()));

		return null; // this will NEVER be reached
	}

	public static MessageTreeAssert treeByTransaction(String type) {
		List<String> types = new ArrayList<String>();

		for (MessageTree tree : new ArrayList<MessageTree>(s_trees)) {
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				if (message.getType().equals(type)) {
					return new MessageTreeAssert(tree);
				} else if (!types.contains(message.getType())) {
					types.add(message.getType());
				}
			}
		}

		Assert.fail(String.format("No message tree(%s) found, but was %s!", type, types.toString()));

		return null; // this will NEVER be reached
	}

	@SuppressWarnings("unchecked")
	public static abstract class AssertSupport<R extends Message, T extends AssertSupport<R, T>> {
		private Message m_message;

		private String m_class;

		public AssertSupport(Message message, String clazz) {
			m_message = message;
			m_class = clazz;
		}

		public T complete() {
			Assert.assertEquals(format("%s is not completed!", m_class), true, m_message.isCompleted());
			return (T) this;
		}

		public T data(String keyValuePairs) {
			Object data = m_message.getData();

			if (data instanceof String) {
				Assert.assertEquals("Data property mismatched!", keyValuePairs, (String) data);
			}

			return (T) this;
		}

		public T data(String key, String value) {
			Object data = m_message.getData();

			if (data instanceof String) {
				Assert.fail(format("No data property(%s) found!", key));
			}

			return (T) this;
		}

		public T name(String name) {
			Assert.assertEquals(format("%s name mismatched!", m_class), name, m_message.getName());
			return (T) this;
		}

		public T notComplete() {
			Assert.assertEquals(format("%s is completed!", m_class), false, m_message.isCompleted());
			return (T) this;
		}

		public T status(String status) {
			Assert.assertEquals(format("%s status mismatched!", m_class), status, m_message.getStatus());
			return (T) this;
		}

		public T success() {
			Assert.assertEquals(format("%s status is not success!", m_class), Message.SUCCESS, m_message.getStatus());
			return (T) this;
		}

		public T type(String type) {
			Assert.assertEquals(format("%s type mismatched!", m_class), type, m_message.getType());
			return (T) this;
		}

		public AssertSupport<R, T> withData() {
			Assert.assertNotNull("Message id is NULL!", m_message.getData());
			return this;
		}
	}

	public static class EventAssert extends AssertSupport<Event, EventAssert> {
		private Event m_event;

		public EventAssert(Event event) {
			super(event, "Event");

			m_event = event;
		}

		public Event event() {
			return m_event;
		}
	}

	public static class HeaderAssert {
		private MessageTree m_tree;

		public HeaderAssert(MessageTree tree) {
			m_tree = tree;
		}

		public HeaderAssert domain(String domain) {
			Assert.assertEquals("Domain mismatched!", domain, m_tree.getDomain());
			return this;
		}

		public HeaderAssert messageId(String messageId) {
			Assert.assertEquals("Message id mismatched!", messageId, m_tree.getMessageId());
			return this;
		}

		public HeaderAssert messageIdStartsWith(String messageIdPrefix) {
			if (!m_tree.getMessageId().startsWith(messageIdPrefix)) {
				Assert.fail(String.format("Message id %s does not start with %s!", m_tree.getMessageId(), messageIdPrefix));
			}

			return this;
		}

		public HeaderAssert parentMessageId(String parentMessageId) {
			Assert.assertEquals("Parent message id mismatched!", parentMessageId, m_tree.getParentMessageId());
			return this;
		}

		public HeaderAssert rootMessageId(String rootMessageId) {
			Assert.assertEquals("Root message id mismatched!", rootMessageId, m_tree.getRootMessageId());
			return this;
		}

		public HeaderAssert withMessageId() {
			Assert.assertNotNull("Message id is NULL!", m_tree.getMessageId());
			return this;
		}

		public HeaderAssert withParentMessageId() {
			Assert.assertNotNull("Message id is NULL!", m_tree.getParentMessageId());
			return this;
		}

		public HeaderAssert withRootMessageId() {
			Assert.assertNotNull("Message id is NULL!", m_tree.getRootMessageId());
			return this;
		}
	}

	public static class HeartbeatAssert extends AssertSupport<Heartbeat, HeartbeatAssert> {
		private Heartbeat m_heartbeat;

		public HeartbeatAssert(Heartbeat heartbeat) {
			super(heartbeat, "Heartbeat");

			m_heartbeat = heartbeat;
		}

		public Heartbeat heartbeat() {
			return m_heartbeat;
		}
	}

	private static class MessageInterceptor extends MessageHandlerAdaptor {
		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		protected void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
			MessageAssert.newTree(tree);
		}
	}

	public static class MessageTreeAssert {
		private MessageTree m_tree;

		public MessageTreeAssert(MessageTree tree) {
			m_tree = tree;
		}

		public EventAssert event() {
			Message message = m_tree.getMessage();

			if (message == null) {
				Assert.fail("No message found!");
			} else if (!(message instanceof Event)) {
				Assert.fail("No event found, but was " + message.getClass().getName());
			}

			return new EventAssert((Event) message);
		}

		public TransactionAssert transaction() {
			Message message = m_tree.getMessage();

			if (message == null) {
				Assert.fail("No message found!");
			} else if (!(message instanceof Transaction)) {
				Assert.fail("No transaction found, but was " + message.getClass().getName());
			}

			return new TransactionAssert((Transaction) message);
		}
	}

	public static class TraceAssert extends AssertSupport<Trace, TraceAssert> {
		private Trace m_trace;

		public TraceAssert(Trace trace) {
			super(trace, "Trace");

			m_trace = trace;
		}

		public Trace trace() {
			return m_trace;
		}
	}

	public static class TransactionAssert extends AssertSupport<Transaction, TransactionAssert> {
		private Transaction m_transaction;

		public TransactionAssert(Transaction transaction) {
			super(transaction, "Transaction");

			m_transaction = transaction;
		}

		public EventAssert childEvent(int index) {
			int count = 0;
			Event event = null;

			for (Message message : m_transaction.getChildren()) {
				if (message instanceof Event) {
					if (count == index) {
						event = (Event) message;
						break;
					}

					count++;
				}
			}

			if (event == null) {
				Assert.fail(format("No child event(%s) found in transaction(%s:%s)!", index, m_transaction.getType(),
				      m_transaction.getName()));
			}

			return new EventAssert(event);
		}

		public List<EventAssert> childEvents() {
			List<EventAssert> events = new ArrayList<EventAssert>();

			for (Message message : m_transaction.getChildren()) {
				if (message instanceof Event) {
					events.add(new EventAssert((Event) message));
				}
			}

			return events;
		}

		public TraceAssert childTrace(int index) {
			int count = 0;
			Trace trace = null;

			for (Message message : m_transaction.getChildren()) {
				if (message instanceof Trace) {
					if (count == index) {
						trace = (Trace) message;
						break;
					}

					count++;
				}
			}

			if (trace == null) {
				Assert.fail(format("No child trace(%s) found in transaction(%s:%s)!", index, m_transaction.getType(),
				      m_transaction.getName()));
			}

			return new TraceAssert(trace);
		}

		public TransactionAssert childTransaction(int index) {
			int count = 0;
			Transaction transaction = null;

			for (Message message : m_transaction.getChildren()) {
				if (message instanceof Transaction) {
					if (count == index) {
						transaction = (Transaction) message;
						break;
					}

					count++;
				}
			}

			if (transaction == null) {
				Assert.fail(format("No child transaction(%s) found in transaction(%s:%s)!", index, m_transaction.getType(),
				      m_transaction.getName()));
			}

			return new TransactionAssert(transaction);
		}

		public List<TransactionAssert> childTransactions() {
			List<TransactionAssert> transactions = new ArrayList<TransactionAssert>();

			for (Message message : m_transaction.getChildren()) {
				if (message instanceof Transaction) {
					transactions.add(new TransactionAssert((Transaction) message));
				}
			}

			return transactions;
		}

		public List<TransactionAssert> childTransactions(String type) {
			List<TransactionAssert> transactions = new ArrayList<TransactionAssert>();

			for (Message message : m_transaction.getChildren()) {
				if (message instanceof Transaction && message.getType().equals(type)) {
					transactions.add(new TransactionAssert((Transaction) message));
				}
			}

			return transactions;
		}

		public TransactionAssert duration(long durationInMillis) {
			String type = m_transaction.getType();
			String name = m_transaction.getName();
			long duration = m_transaction.getDurationInMillis();

			Assert.assertEquals(format("Duration mismatched in transaction(%s:%s)!", type, name), durationInMillis,
			      duration);
			return this;
		}

		public void noChild() {
			String type = m_transaction.getType();
			String name = m_transaction.getName();
			int size = m_transaction.getChildren().size();

			Assert.assertEquals(format("No child expected under transaction(%s:%s)!", type, name), 0, size);
		}

		public Transaction transaction() {
			return m_transaction;
		}
	}
}
