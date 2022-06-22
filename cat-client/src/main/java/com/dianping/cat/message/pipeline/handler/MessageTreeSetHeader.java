package com.dianping.cat.message.pipeline.handler;

import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.message.MessageTree;
import com.dianping.cat.message.context.TraceContextHelper;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

// Component
public class MessageTreeSetHeader extends MessageHandlerAdaptor {
	public static String ID = "message-tree-set-header";

	@Override
	public int getOrder() {
		return 100;
	}

	@Override
	protected void handleMessagreTree(MessageHandlerContext ctx, MessageTree tree) {
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
			tree.setMessageId(TraceContextHelper.createMessageId());
		}

		if (tree.getThreadId() == null) {
			Thread thread = Thread.currentThread();

			tree.setThreadId(String.valueOf(thread.getId()));
			tree.setThreadName(thread.getName());
			tree.setThreadGroupName(thread.getThreadGroup().getName());
		}

		ctx.fireMessage(tree);
	}
}
