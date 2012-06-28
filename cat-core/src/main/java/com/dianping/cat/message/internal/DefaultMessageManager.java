package com.dianping.cat.message.internal;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.status.StatusUpdateTask;
import com.site.helper.Threads;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultMessageManager extends ContainerHolder implements MessageManager, LogEnabled {
	@Inject
	private MessageStatistics m_statistics;

	private StatusUpdateTask m_statusUpdateTask;

	private MessageIdFactory m_factory;

	private TransportManager m_manager;

	// we don't use static modifier since MessageManager is a singleton in
	// production actually
	private ThreadLocal<Context> m_context = new ThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return null;
		}
	};

	private ClientConfig m_clientConfig;

	private ClientConfig m_serverConfig;

	private Domain m_domain;

	private String m_hostName;

	private Logger m_logger;

	private boolean m_firstMessage = true;

	@Override
	public void add(Message message) {
		Context ctx = getContext();

		if (ctx != null) {
			ctx.add(this, message);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void end(Transaction transaction) {
		Context ctx = getContext();

		if (ctx != null && transaction.isStandalone()) {
			if (ctx.end(this, transaction)) {
				m_context.remove();
			}
		}
	}

	void flush(MessageTree tree) {
		if (m_manager != null) {
			MessageSender sender = m_manager.getSender();

			if (sender != null && !shouldThrottle(tree)) {
				sender.send(tree);

				if (m_statistics != null) {
					m_statistics.onSending(tree);
				}
			}
		}
	}

	@Override
	public ClientConfig getClientConfig() {
		return m_clientConfig;
	}

	Context getContext() {
		if (Cat.isInitialized()) {
			Context ctx = m_context.get();

			if (ctx != null) {
				return ctx;
			} else if (m_clientConfig.isDevMode()) {
				throw new RuntimeException("Cat has not been initialized successfully, "
				      + "please call Cal.setup(...) first for each thread.");
			}
		}

		return null;
	}

	@Override
	public Transaction getPeekTransaction() {
		Context ctx = getContext();

		if (ctx != null) {
			return ctx.peekTransaction(this);
		} else {
			return null;
		}
	}

	@Override
	public ClientConfig getServerConfig() {
		return m_serverConfig;
	}

	@Override
	public MessageTree getThreadLocalMessageTree() {
		Context ctx = m_context.get();

		if (ctx != null) {
			return ctx.m_tree;
		} else {
			return null;
		}
	}

	@Override
	public boolean hasContext() {
		return m_context.get() != null;
	}

	@Override
	public void initializeClient(ClientConfig clientConfig) {
		if (clientConfig != null) {
			m_clientConfig = clientConfig;
		} else {
			m_clientConfig = new ClientConfig();
			m_clientConfig.setMode("client");
		}

		Map<String, Domain> domains = m_clientConfig.getDomains();
		Domain firstDomain = domains.isEmpty() ? null : domains.values().iterator().next();

		m_domain = firstDomain == null ? new Domain("unknown").setEnabled(false) : firstDomain;

		m_hostName = NetworkInterfaceManager.INSTANCE.getLocalHostName();

		if (m_domain.getIp() == null) {
			m_domain.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		}

		// initialize milli-second resolution level timer
		MilliSecondTimer.initialize();

		m_manager = lookup(TransportManager.class);
		m_factory = lookup(MessageIdFactory.class);
		m_statusUpdateTask = lookup(StatusUpdateTask.class);

		// initialize domain and ip address
		m_factory.initialize(m_domain.getId());

		// start status update task
		Threads.forGroup("Cat").start(m_statusUpdateTask);
	}

	@Override
	public void initializeServer(ClientConfig serverConfig) {
		m_serverConfig = serverConfig;
	}

	@Override
	public boolean isCatEnabled() {
		return m_domain != null && m_domain.isEnabled() && m_context.get() != null;
	}

	String nextMessageId() {
		return m_factory.getNextId();
	}

	@Override
	public void reset() {
		// destroy current thread local data
		Context ctx = m_context.get();

		if (ctx != null) {
			ctx.m_stack.clear();
		}

		m_context.remove();
	}

	@Override
	public void setup() {
		Context ctx;

		if (m_domain != null) {
			ctx = new Context(m_domain.getId(), m_hostName, m_domain.getIp());
		} else {
			ctx = new Context("Unknown", m_hostName, "");
		}

		m_context.set(ctx);
	}

	boolean shouldThrottle(MessageTree tree) {
		if(!isCatEnabled()){
			return true;
		}
		
		if (tree.getMessage() != null && "Heartbeat".equals(tree.getMessage().getName())) {
			return false;
		}
		
		int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

		return threadCount > m_domain.getMaxThreads();
	}

	@Override
	public void start(Transaction transaction) {
		Context ctx = getContext();

		if (ctx != null) {
			ctx.start(this, transaction);
		} else if (m_firstMessage) {
			m_firstMessage = false;
			m_logger.warn("CAT client is not enabled because it's not initialized yet");
		}
	}

	static class Context {
		private MessageTree m_tree;

		private Stack<Transaction> m_stack;

		public Context(String domain, String hostName, String ipAddress) {
			m_tree = new DefaultMessageTree();
			m_stack = new Stack<Transaction>();
			Thread thread = Thread.currentThread();
			String groupName = thread.getThreadGroup().getName();

			m_tree.setThreadGroupName(groupName);
			m_tree.setThreadId(String.valueOf(thread.getId()));
			m_tree.setThreadName(thread.getName());

			m_tree.setDomain(domain);
			m_tree.setHostName(hostName);
			m_tree.setIpAddress(ipAddress);
		}

		public void add(DefaultMessageManager manager, Message message) {
			if (m_stack.isEmpty()) {
				MessageTree tree = m_tree.copy();

				tree.setMessageId(manager.nextMessageId());
				tree.setMessage(message);
				manager.flush(tree);
			} else {
				Transaction entry = m_stack.peek();

				entry.addChild(message);
			}
		}

		/**
		 * return true means the transaction has been flushed.
		 * 
		 * @param manager
		 * @param transaction
		 * @return true if message is flushed, false otherwise
		 */
		public boolean end(DefaultMessageManager manager, Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction current = m_stack.pop();

				if (transaction == current) {
					validateTransaction(current);
				} else {
					while (transaction != current && !m_stack.empty()) {
						validateTransaction(current);

						current = m_stack.pop();
					}
				}

				if (m_stack.isEmpty()) {
					MessageTree tree = m_tree.copy();

					m_tree.setMessageId(null);
					m_tree.setMessage(null);
					manager.flush(tree);
					return true;
				}
			}

			return false;
		}

		public Transaction peekTransaction(DefaultMessageManager defaultMessageManager) {
			if (m_stack.isEmpty()) {
				return null;
			} else {
				return m_stack.peek();
			}
		}

		public void start(DefaultMessageManager manager, Transaction transaction) {
			if (!m_stack.isEmpty()) {
				Transaction entry = m_stack.peek();

				entry.addChild(transaction);
			} else {
				m_tree.setMessageId(manager.nextMessageId());
				m_tree.setMessage(transaction);
			}

			m_stack.push(transaction);
		}

		void validateTransaction(Transaction transaction) {
			if (!transaction.isStandalone()) {
				return;
			}

			List<Message> children = transaction.getChildren();
			int len = children.size();

			for (int i = 0; i < len; i++) {
				Message message = children.get(i);

				if (message instanceof Transaction) {
					validateTransaction((Transaction) message);
				}
			}

			if (!transaction.isCompleted() && transaction.isStandalone() && transaction instanceof DefaultTransaction) {
				// missing transaction end, log a BadInstrument event so that
				// developer can fix the code
				DefaultEvent notCompleteEvent = new DefaultEvent("CAT", "BadInstrument");

				notCompleteEvent.setStatus("TransactionNotCompleted");
				notCompleteEvent.setCompleted(true);
				transaction.addChild(notCompleteEvent);
				((DefaultTransaction) transaction).setCompleted(true);
			}
		}
	}
}
