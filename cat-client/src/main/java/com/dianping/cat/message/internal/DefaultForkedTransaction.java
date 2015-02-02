package com.dianping.cat.message.internal;

import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultForkedTransaction extends DefaultTransaction implements ForkedTransaction {
	private String m_rootMessageId;

	private String m_parentMessageId;

	private String m_forkedMessageId;

	public DefaultForkedTransaction(String type, String name, MessageManager manager) {
		super(type, name, manager);

		setStandalone(false);

		MessageTree tree = manager.getThreadLocalMessageTree();

		if (tree != null) {
			m_rootMessageId = tree.getRootMessageId();
			m_parentMessageId = tree.getMessageId();
		}
	}

	@Override
	public void fork() {
		MessageManager manager = getManager();

		manager.setup();
		manager.start(this, false);

		MessageTree tree = manager.getThreadLocalMessageTree();

		if (tree != null) {
			m_forkedMessageId = tree.getMessageId();
			tree.setRootMessageId(m_rootMessageId == null ? m_parentMessageId : m_rootMessageId);
			tree.setParentMessageId(m_parentMessageId);
		}
	}

	@Override
	public String getForkedMessageId() {
		return m_forkedMessageId;
	}
}
