package com.dianping.cat.message.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Stack;
import java.util.UUID;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.io.MessageSender;
import com.dianping.cat.message.io.TransportManager;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ContainerHolder;

public class DefaultMessageManager extends ContainerHolder implements MessageManager, LogEnabled {
	private TransportManager m_manager;

	// we don't use static modifier since MessageManager is a singleton in
	// production actually
	private InheritableThreadLocal<Context> m_context = new InheritableThreadLocal<Context>() {
		@Override
		protected Context initialValue() {
			return null;
		}
	};

	private Config m_clientConfig;

	private Config m_serverConfig;

	private String m_domain;

	private String m_hostName;

	private String m_ipAddress;

	private Logger m_logger;

	private boolean m_firstMessage = true;

	@Override
	public void add(Message message) {
		if (Cat.isInitialized()) {
			getContext().add(this, message);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void end(Transaction transaction) {
		if (Cat.isInitialized()) {
			getContext().end(this, transaction);
		}
	}

	void flush(MessageTree tree) {
		if (m_manager != null) {
			MessageSender sender = m_manager.getSender();

			if (sender != null) {
				sender.send(tree);
			}
		}
	}

	@Override
	public Config getClientConfig() {
		return m_clientConfig;
	}

	Context getContext() {
		Context ctx = m_context.get();

		if (ctx == null) {
			throw new RuntimeException("Cat has not been initialized successfully, "
			      + "please call Cal.setup(...) first for each thread.");
		} else {
			return ctx;
		}
	}

	@Override
	public Config getServerConfig() {
		return m_serverConfig;
	}

	@Override
	public void initializeClient(Config clientConfig) {
		if (clientConfig != null) {
			m_clientConfig = clientConfig;
		} else {
			m_clientConfig = new Config();
			m_clientConfig.setMode("client");
		}

		if (m_clientConfig.getApp() != null) {
			m_domain = m_clientConfig.getApp().getDomain();
		}

		try {
			InetAddress localHost = InetAddress.getLocalHost();

			m_hostName = localHost.getHostName();
			m_ipAddress = localHost.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		m_manager = lookup(TransportManager.class);
	}

	@Override
	public void initializeServer(Config serverConfig) {
		m_serverConfig = serverConfig;
	}

	@Override
	public void reset() {
		// destroy current thread local data
		m_context.remove();
	}

	@Override
	public void setup(String sessionToken, String requestToken) {
		Context ctx = new Context(m_domain, m_hostName, m_ipAddress, sessionToken, requestToken);

		m_context.set(ctx);
	}

	@Override
	public void start(Transaction transaction) {
		if (Cat.isInitialized()) {
			getContext().start(transaction);
		} else if (m_firstMessage){
			m_firstMessage = false;
			m_logger.warn("CAT client is not enabled because it's not initialized yet");
		}
	}

	static class Context {
		private MessageTree m_tree;

		private Stack<Transaction> m_stack;

		public Context(String domain, String hostName, String ipAddress, String sessionToken, String requestToken) {
			m_tree = new DefaultMessageTree();
			m_stack = new Stack<Transaction>();

			m_tree.setDomain(domain);
			m_tree.setSessionToken(sessionToken);
			m_tree.setRequestToken(requestToken);

			Thread thread = Thread.currentThread();

			m_tree.setThreadId(Long.toHexString(thread.getId()));
			m_tree.setThreadId(thread.getName());

			m_tree.setHostName(hostName);
			m_tree.setIpAddress(ipAddress);
			m_tree.setMessageId(UUID.randomUUID().toString()); // TODO optimize it
			                                                   // to shorter UUID
		}

		public void add(DefaultMessageManager manager, Message message) {
			if (m_stack.isEmpty()) {
				m_tree.setMessage(message);
				manager.flush(m_tree);
			} else {
				Transaction entry = m_stack.peek();

				entry.addChild(message);
			}
		}

		public void end(DefaultMessageManager manager, Transaction transaction) {
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
			} else {
				m_tree.setMessage(transaction);
			}

			m_stack.push(transaction);
		}

		void validateTransaction(Transaction transaction) {
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
}
