package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.message.io.MessageTreePool;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;
import com.dianping.cat.message.tree.MessageIdFactory;
import com.dianping.cat.message.tree.MessageTree;

// Component
public class MessageTreeSetHeader extends MessageHandlerAdaptor implements Initializable {
	public static String ID = "set-header";

	// Inject
	private MessageIdFactory m_factory;

	private MessageTreePool m_pool;

	@Override
	public int getOrder() {
		return 100;
	}

	@Override
	public void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
		ConfigureManager manager = ctx.getConfigureManager();

		if (tree.getDomain() == null) {
			tree.setDomain(manager.getDomain());
		}

		if (tree.getIpAddress() == null) {
			tree.setIpAddress(manager.getHost().getIp());
		}

		if (tree.getHostName() == null) {
			tree.setHostName(manager.getHost().getName());
		}

		if (tree.getMessageId() == null) {
			tree.setMessageId(m_factory.getNextId());
		}

		if (tree.getThreadId() == null) {
			Thread thread = Thread.currentThread();

			tree.setThreadId(String.valueOf(thread.getId()));
			tree.setThreadName(thread.getName());
			tree.setThreadGroupName(thread.getThreadGroup().getName());
		}

		ctx.fireMessage(tree);
		m_pool.feed(tree);
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_factory = ctx.lookup(MessageIdFactory.class);
		m_pool = ctx.lookup(MessageTreePool.class);
	}
}
